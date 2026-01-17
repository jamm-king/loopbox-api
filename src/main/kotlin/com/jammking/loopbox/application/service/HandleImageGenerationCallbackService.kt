package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.exception.ImageAiClientException
import com.jammking.loopbox.application.exception.ImageFileStorageException
import com.jammking.loopbox.application.port.`in`.HandleImageGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.ImageFileStorage
import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.exception.image.ImageTaskInconsistentStateException
import com.jammking.loopbox.domain.exception.image.InvalidImageStateException
import com.jammking.loopbox.domain.exception.project.ProjectImageInconsistentStateException
import com.jammking.loopbox.domain.exception.task.ImageGenerationTaskNotFoundException
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class HandleImageGenerationCallbackService(
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository,
    private val versionRepository: ImageVersionRepository,
    private val taskRepository: ImageGenerationTaskRepository,
    private val fileRepository: ImageFileRepository,
    private val notificationPort: NotificationPort,
    private val imageFileStorage: ImageFileStorage
): HandleImageGenerationCallbackUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(command: HandleImageGenerationCallbackUseCase.Command) {
        command.validateProtocol()

        val provider = command.provider
        val externalId = command.externalId

        val task = requireTaskByProviderAndExternalId(provider, externalId)
        if (task.isCanceled()) {
            log.info(
                "Ignoring callback for canceled task: taskId={}, provider={}, externalId={}, status={}",
                task.id.value, provider, externalId.value, command.status
            )
            return
        }
        val image = requireImageByTask(task)
        val project = requireProjectByImage(image)

        if (image.isFailed()) {
            log.info(
                "Ignoring callback because image is FAILED (requires user ack): imageId={}, taskId={}, callbackStatus={}, message={}",
                image.id.value, task.id.value, command.status, command.message
            )
            return
        }
        if (!image.isGenerating()) {
            log.info(
                "Ignoring callback because image is not GENERATING: imageId={}, imageStatus={}, taskId={}, taskStatus={}, callbackStatus={}",
                image.id.value, image.status, task.id.value, task.status, command.status
            )
            return
        }

        when(command.status) {
            HandleImageGenerationCallbackUseCase.Command.Status.COMPLETED ->
                handleCompleted(command, project, image, task)
            HandleImageGenerationCallbackUseCase.Command.Status.FAILED ->
                handleFailed(command, project, image, task)
            HandleImageGenerationCallbackUseCase.Command.Status.GENERATING -> {
                log.info(
                    "Image generation is on process: imageId={}, taskId={}, message={}",
                    image.id.value, task.id.value, command.message
                )
                return
            }
            HandleImageGenerationCallbackUseCase.Command.Status.UNKNOWN -> {
                log.warn(
                    "Unknown image generation callback: imageId={}, taskId={}, message={}",
                    image.id.value, task.id.value, command.message
                )
                return
            }
        }
    }

    private fun handleCompleted(
        command: HandleImageGenerationCallbackUseCase.Command,
        project: Project,
        image: Image,
        task: ImageGenerationTask
    ) {
        val images = command.images
            ?: throw ImageAiClientException.invalidPayloadState(command.provider)

        val config = image.requestedConfig
            ?: throw InvalidImageStateException(image, "create new version (requestedConfig is null")

        val savedVersions = images.map { asset ->
            val version = versionRepository.save(
                ImageVersion(
                    imageId = image.id,
                    config = config,
                    createdAt = Instant.now()
                )
            )

            version.startFileDownload()
            versionRepository.save(version)

            try {
                val filePath = imageFileStorage.saveFromRemoteUrl(
                    remoteUrl = asset.remoteUrl,
                    projectId = project.id,
                    imageId = image.id,
                    versionId = version.id
                )
                val savedFile = fileRepository.save(ImageFile(path = filePath))
                version.completeFileDownload(savedFile.id)
            } catch(e: ImageFileStorageException) {
                log.warn(
                    "Image file download failed: imageId={}, versionId={}, remoteUrl={}, reason={}",
                    image.id.value, version.id.value, asset.remoteUrl, e.message
                )
                version.failFileDownload()
            }

            versionRepository.save(version)
        }

        image.completeVersionGeneration()
        task.markCompleted()

        val savedImage = imageRepository.save(image)
        val savedTask = taskRepository.save(task)
        log.info(
            "Completed image generation: imageId={}, taskId={}, versionCount={}",
            savedImage.id.value, savedTask.id.value, savedVersions.size
        )

        notificationPort.notifyImageVersionGenerationCompleted(
            projectId = project.id,
            imageId = savedImage.id,
            versionIds = savedVersions.map { it.id }
        )
        log.info(
            "Notified image generation completion: projectId={}, imageId={}",
            project.id.value, savedImage.id.value
        )
    }

    private fun handleFailed(
        command: HandleImageGenerationCallbackUseCase.Command,
        project: Project,
        image: Image,
        task: ImageGenerationTask
    ) {
        val config = image.requestedConfig
            ?: throw InvalidImageStateException(image, "create failed version (requestedConfig is null")

        val failedVersion = ImageVersion(
            status = ImageVersionStatus.GENERATION_FAILED,
            config = config,
            imageId = image.id,
            createdAt = Instant.now()
        )

        image.failVersionGeneration()
        task.markFailed(command.message)

        val savedImage = imageRepository.save(image)
        val savedVersion = versionRepository.save(failedVersion)
        val savedTask = taskRepository.save(task)

        log.info(
            "Failed image generation: imageId={}, taskId={}, versionId={}, message={}",
            savedImage.id.value, savedTask.id.value, savedVersion.id.value, command.message
        )

        notificationPort.notifyImageVersionGenerationFailed(
            projectId = project.id,
            imageId = image.id
        )
        log.info(
            "Notified image generation failure: projectId={}, imageId={}",
            project.id.value, image.id.value
        )
    }

    private fun requireTaskByProviderAndExternalId(provider: ImageAiProvider, externalId: ExternalId): ImageGenerationTask {
        val task = taskRepository.findByProviderAndExternalId(provider, externalId)
        if (task == null) {
            log.warn(
                "Task not found for callback: provider={}, externalId={}",
                provider, externalId
            )
            throw ImageGenerationTaskNotFoundException.byProviderAndExternalId(provider, externalId)
        }
        return task
    }

    private fun requireImageByTask(task: ImageGenerationTask): Image {
        val image = imageRepository.findById(task.imageId)
        if (image == null) {
            log.error(
                "Inconsistent state: Image not found for existing task: taskId={}, imageId={}",
                task.id.value, task.imageId.value
            )
            throw ImageTaskInconsistentStateException.imageMissingForTask(task.id, task.imageId)
        }
        return image
    }

    private fun requireProjectByImage(image: Image): Project {
        val project = projectRepository.findById(image.projectId)
        if (project == null) {
            log.error(
                "Inconsistent state: Project not found for existing image: imageId={}, projectId={}",
                image.id.value, image.projectId.value
            )
            throw ProjectImageInconsistentStateException.projectMissingForImage(image.id, image.projectId)
        }
        return project
    }
}
