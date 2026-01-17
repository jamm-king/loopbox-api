package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ImageManagementUseCase
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.exception.image.ImageNotFoundException
import com.jammking.loopbox.domain.exception.image.ImageVersionNotFoundException
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectImageInconsistentStateException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ImageManagementService(
    private val projectRepository: ProjectRepository,
    private val imageRepository: ImageRepository,
    private val versionRepository: ImageVersionRepository,
    private val taskRepository: ImageGenerationTaskRepository,
    private val imageAiRouter: ImageAiRouter
): ImageManagementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun createImage(userId: UserId, projectId: ProjectId): Image {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)

        val image = Image(projectId = projectId)
        val saved = imageRepository.save(image)
        log.info("Created image: ${saved.id.value}")

        return saved
    }

    override fun deleteImage(userId: UserId, imageId: ImageId) {
        val image = imageRepository.findById(imageId)
            ?: throw ImageNotFoundException.byImageId(imageId)
        val project = projectRepository.findById(image.projectId)
            ?: throw ProjectImageInconsistentStateException.projectMissingForImage(image.id, image.projectId)
        requireOwner(project, userId)

        val tasks = taskRepository.findByImageId(image.id)
        tasks.forEach { task ->
            task.markCanceled()
            taskRepository.save(task)
        }

        imageRepository.deleteById(image.id)
        versionRepository.deleteByImageId(image.id)
        log.info("Deleted image: ${image.id.value}")
    }

    override fun generateVersion(command: ImageManagementUseCase.GenerateVersionCommand): Image {
        val imageId = command.imageId
        val config = command.config
        val provider = command.provider

        val image = imageRepository.findById(imageId)
            ?: throw ImageNotFoundException.byImageId(imageId)

        val project = projectRepository.findById(image.projectId)
            ?: throw ProjectImageInconsistentStateException.projectMissingForImage(image.id, image.projectId)
        requireOwner(project, command.userId)

        image.startVersionGeneration(config)
        val savedImage = imageRepository.save(image)

        val imageAiClient = imageAiRouter.getClient(provider)

        log.info(
            "Requesting version generation: projectId={}, imageId={}, provider={}, title='{}', config={}",
            project.id.value, image.id.value, provider, project.title, config
        )

        return try {
            val result = imageAiClient.generate(
                ImageAiClient.GenerateCommand(config = config)
            )

            val task = ImageGenerationTask(
                imageId = image.id,
                provider = provider,
                externalId = result.externalId
            )
            task.markGenerating()
            val savedTask = taskRepository.save(task)
            log.info(
                "Requested version generation to AI: projectId={}, imageId={}, taskId={}",
                project.id.value, image.id.value, savedTask.id.value
            )

            savedImage
        } catch(e: Exception) {
            log.error("Failed to request version generation: imageId={}, reason={}", savedImage.id.value, e.message, e)

            savedImage.failVersionGeneration()
            imageRepository.save(savedImage)

            throw e
        }
    }

    override fun deleteVersion(userId: UserId, imageId: ImageId, versionId: ImageVersionId): Image {
        val image = imageRepository.findById(imageId)
            ?: throw ImageNotFoundException.byImageId(imageId)
        val project = projectRepository.findById(image.projectId)
            ?: throw ProjectImageInconsistentStateException.projectMissingForImage(image.id, image.projectId)
        requireOwner(project, userId)
        val version = versionRepository.findById(versionId)
            ?: throw ImageVersionNotFoundException.byVersionId(versionId)

        image.startVersionDeletion()

        return try {
            versionRepository.deleteById(versionId)
            image.completeVersionDeletion()
            val saved = imageRepository.save(image)
            log.info("Deleted image version: imageId=${imageId.value}, versionId=${versionId.value}")

            saved
        } catch(e: Exception) {
            image.failVersionDeletion()
            val saved = imageRepository.save(image)
            log.error("Failed to delete image version: imageId=${imageId.value}, versionId=${versionId.value}")

            saved
        }
    }

    override fun acknowledgeFailure(userId: UserId, imageId: ImageId): Image {
        val image = imageRepository.findById(imageId)
            ?: throw ImageNotFoundException.byImageId(imageId)
        val project = projectRepository.findById(image.projectId)
            ?: throw ProjectImageInconsistentStateException.projectMissingForImage(image.id, image.projectId)
        requireOwner(project, userId)

        image.acknowledgeFailure()
        val saved = imageRepository.save(image)
        log.info("Acknowledged image's failure status: imageId=${imageId.value}")

        return saved
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
