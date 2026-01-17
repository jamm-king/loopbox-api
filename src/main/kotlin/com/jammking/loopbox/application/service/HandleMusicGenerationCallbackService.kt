package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.exception.MusicAiClientException
import com.jammking.loopbox.application.exception.MusicFileStorageException
import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.MusicFileStorage
import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.exception.music.InvalidMusicStateException
import com.jammking.loopbox.domain.exception.music.MusicTaskInconsistentStateException
import com.jammking.loopbox.domain.exception.project.ProjectMusicInconsistentStateException
import com.jammking.loopbox.domain.exception.task.MusicGenerationTaskNotFoundException
import com.jammking.loopbox.domain.port.out.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class HandleMusicGenerationCallbackService(
    private val projectRepository: ProjectRepository,
    private val musicRepository: MusicRepository,
    private val versionRepository: MusicVersionRepository,
    private val taskRepository: MusicGenerationTaskRepository,
    private val fileRepository: AudioFileRepository,
    private val notificationPort: NotificationPort,
    private val musicFileStorage: MusicFileStorage
): HandleMusicGenerationCallbackUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(command: HandleMusicGenerationCallbackUseCase.Command) {

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
        val music = requireMusicByTask(task)
        val project = requireProjectByMusic(music)

        if (music.isFailed()) {
            log.info(
                "Ignoring callback because music is FAILED (requires user ack): musicId={}, taskId={}, callbackStatus={}, message={}",
                music.id.value, task.id.value, command.status, command.message
            )
            return
        }
        if (!music.isGenerating()) {
            log.info(
                "Ignoring callback because music is not GENERATING: musicId={}, musicStatus={}, taskId={}, taskStatus={}, callbackStatus={}",
                music.id.value, music.status, task.id.value, task.status, command.status
            )
            return
        }

        when(command.status) {
            HandleMusicGenerationCallbackUseCase.Command.Status.COMPLETED -> handleCompleted(command, project, music, task)
            HandleMusicGenerationCallbackUseCase.Command.Status.FAILED -> handleFailed(command, project, music, task)
            HandleMusicGenerationCallbackUseCase.Command.Status.GENERATING -> {
                log.info(
                    "Music generation is on process: musicId={}, taskId={}, message={}",
                    music.id.value, task.id.value, command.message
                )
                return
            }
            HandleMusicGenerationCallbackUseCase.Command.Status.UNKNOWN -> {
                log.warn(
                    "Unknown music generation callback: musicId={}, taskId={}, message={}",
                    music.id.value, task.id.value, command.message
                )
                return
            }
        }
    }

    private fun handleCompleted(
        command: HandleMusicGenerationCallbackUseCase.Command,
        project: Project,
        music: Music,
        task: MusicGenerationTask
    ) {
        val tracks = command.tracks
            ?: throw MusicAiClientException.invalidPayloadState(command.provider)

        val config = music.requestedConfig
            ?: throw InvalidMusicStateException(music, "create new version (requestedConfig is null")

        val savedVersions = tracks.map { track ->
            val version = versionRepository.save(
                MusicVersion(
                    musicId = music.id,
                    config = config,
                    durationSeconds = track.durationSeconds,
                    createdAt = Instant.now()
                )
            )

            version.startFileDownload()
            versionRepository.save(version)

            try {
                val filePath = musicFileStorage.saveFromRemoteUrl(
                    remoteUrl = track.remoteUrl,
                    projectId = project.id,
                    musicId = music.id,
                    versionId = version.id
                )
                val savedFile = fileRepository.save(AudioFile(path = filePath))
                version.completeFileDownload(savedFile.id)
            } catch(e: MusicFileStorageException) {
                log.warn(
                    "Audio file download failed: musicId={}, versionId={}, remoteUrl={}, reason={}",
                    music.id.value, version.id.value, track.remoteUrl, e.message
                )
                version.failFileDownload()
            }

            versionRepository.save(version)
        }

        music.completeVersionGeneration()
        task.markCompleted()
        project.markMusicReady()

        val savedMusic = musicRepository.save(music)
        val savedTask = taskRepository.save(task)
        val savedProject = projectRepository.save(project)
        log.info(
            "Completed music generation: musicId={}, taskId={}, versionCount={}",
            savedMusic.id.value, savedTask.id.value, savedVersions.size
        )

        notificationPort.notifyVersionGenerationCompleted(
            projectId = savedProject.id,
            musicId = savedMusic.id,
            versionIds = savedVersions.map { it.id }
        )
        log.info(
            "Notified music generation completion: projectId={}, musicId={}",
            savedProject.id.value, savedMusic.id.value
        )
    }

    private fun handleFailed(
        command: HandleMusicGenerationCallbackUseCase.Command,
        project: Project,
        music: Music,
        task: MusicGenerationTask
    ) {
        val config = music.requestedConfig
            ?: throw InvalidMusicStateException(music, "create failed version (requestedConfig is null")

        val failedVersion = MusicVersion(
            status = MusicVersionStatus.GENERATION_FAILED,
            config = config,
            musicId = music.id,
            createdAt = Instant.now()
        )

        music.failVersionGeneration()
        task.markFailed(command.message)

        val savedMusic = musicRepository.save(music)
        val savedVersion = versionRepository.save(failedVersion)
        val savedTask = taskRepository.save(task)

        log.info(
            "Failed music generation: musicId={}, taskId={}, versionId={}, message={}",
            savedMusic.id.value, savedTask.id.value, savedVersion.id.value, command.message
        )

        notificationPort.notifyVersionGenerationFailed(
            projectId = project.id,
            musicId = music.id
        )
        log.info(
            "Notified music generation failure: projectId={}, musicId={}",
            project.id.value, music.id.value
        )
    }

    private fun requireTaskByProviderAndExternalId(provider: MusicAiProvider, externalId: ExternalId): MusicGenerationTask {
        val task = taskRepository.findByProviderAndExternalId(provider, externalId)
        if (task == null) {
            log.warn(
                "Task not found for callback: provider={}, externalId={}",
                provider, externalId
            )
            throw MusicGenerationTaskNotFoundException.byProviderAndExternalId(provider, externalId)
        }
        return task
    }

    private fun requireMusicByTask(task: MusicGenerationTask): Music {
        val music = musicRepository.findById(task.musicId)
        if (music == null) {
            log.error(
                "Inconsistent state: Music not found for existing task: taskId={}, musicId={}",
                task.id.value, task.musicId.value
            )
            throw MusicTaskInconsistentStateException.musicMissingForTask(task.id, task.musicId)
        }
        return music
    }

    private fun requireProjectByMusic(music: Music): Project {
        val project = projectRepository.findById(music.projectId)
        if (project == null) {
            log.error(
                "Inconsistent state: Project not found for existing music: musicId={}, projectId={}",
                music.id.value, music.projectId.value
            )
            throw ProjectMusicInconsistentStateException.projectMissingForMusic(music.id, music.projectId)
        }
        return project
    }
}
