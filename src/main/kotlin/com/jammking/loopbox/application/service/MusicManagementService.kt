package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.application.port.out.MusicAiRouter
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicOperation
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.exception.music.MusicNotFoundException
import com.jammking.loopbox.domain.exception.music.MusicVersionNotFoundException
import com.jammking.loopbox.domain.exception.project.ProjectMusicInconsistentStateException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MusicManagementService(
    private val projectRepository: ProjectRepository,
    private val musicRepository: MusicRepository,
    private val versionRepository: MusicVersionRepository,
    private val taskRepository: MusicGenerationTaskRepository,
    private val musicAiRouter: MusicAiRouter
): MusicManagementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun createMusic(projectId: ProjectId, alias: String?): Music {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)

        val music = Music(projectId = projectId, alias = alias)
        val saved = musicRepository.save(music)
        log.info("Created music: ${saved.id.value}")

        return saved
    }

    override fun updateMusic(command: MusicManagementUseCase.UpdateMusicCommand): Music {
        val music = musicRepository.findById(command.musicId)
            ?: throw MusicNotFoundException.byMusicId(command.musicId)

        val normalizedAlias = command.alias?.trim()?.ifEmpty { null }
        music.updateAlias(normalizedAlias)

        val saved = musicRepository.save(music)
        log.info("Updated music alias: musicId={}, alias={}", saved.id.value, saved.alias)

        return saved
    }

    override fun deleteMusic(musicId: MusicId) {
        val music = musicRepository.findById(musicId)
            ?: throw MusicNotFoundException.byMusicId(musicId)
        val project = projectRepository.findById(music.projectId)
            ?: throw ProjectMusicInconsistentStateException.projectMissingForMusic(music.id, music.projectId)

        val tasks = taskRepository.findByMusicId(music.id)
        tasks.forEach { task ->
            task.markCanceled()
            taskRepository.save(task)
        }

        musicRepository.deleteById(music.id)
        versionRepository.deleteByMusicId(music.id)
        log.info("Deleted music: ${music.id.value}")

        val musicList = musicRepository.findByProjectId(music.projectId)
        if(musicList.isEmpty()) {
            project.markDraft()
            val saved = projectRepository.save(project)
            log.info("Changed project status: projectId=${saved.id}, status=${saved.status}")
        }
    }

    override fun generateVersion(
        command: MusicManagementUseCase.GenerateVersionCommand
    ): Music {
        val musicId = command.musicId
        val config = command.config
        val provider = command.provider

        val music = musicRepository.findById(musicId)
            ?: throw MusicNotFoundException.byMusicId(musicId)

        val project = projectRepository.findById(music.projectId)
            ?: throw ProjectMusicInconsistentStateException.projectMissingForMusic(music.id, music.projectId)

        music.startVersionGeneration(config)
        val savedMusic = musicRepository.save(music)

        val musicAiClient = musicAiRouter.getClient(provider)

        log.info(
            "Requesting version generation: projectId={}, musicId={}, provider={}, title='{}', config={}",
            project.id.value, music.id.value, provider, project.title, config
        )

        return try {
            val result = musicAiClient.generate(
                MusicAiClient.GenerateCommand(title = project.title, config = config)
            )

            val task = MusicGenerationTask(
                musicId = music.id,
                provider = provider,
                externalId = result.externalId
            )
            task.markGenerating()
            val savedTask = taskRepository.save(task)
            log.info(
                "Requested version generation to AI: projectId={}, musicId={}, taskId={}",
                project.id.value, music.id.value, savedTask.id.value
            )

            savedMusic
        } catch(e: Exception) {
            log.error("Failed to request version generation: musicId={}, reason={}", savedMusic.id.value, e.message, e)

            savedMusic.failVersionGeneration()
            musicRepository.save(savedMusic)

            throw e
        }
    }

    override fun deleteVersion(musicId: MusicId, versionId: MusicVersionId): Music {
        val music = musicRepository.findById(musicId)
            ?: throw MusicNotFoundException.byMusicId(musicId)
        val version = versionRepository.findById(versionId)
            ?: throw MusicVersionNotFoundException.byVersionId(versionId)

        music.startVersionDeletion()

        return try {
            versionRepository.deleteById(versionId)
            music.completeVersionDeletion()
            val saved = musicRepository.save(music)
            log.info("Deleted music version: musicId=${musicId.value}, versionId=${versionId.value}")

            saved
        } catch(e: Exception) {
            music.failVersionDeletion()
            val saved = musicRepository.save(music)
            log.error("Failed to delete music version: musicId=${musicId.value}, versionId=${versionId.value}")

            saved
        }
    }

    override fun acknowledgeFailure(musicId: MusicId): Music {
        val music = musicRepository.findById(musicId)
            ?: throw MusicNotFoundException.byMusicId(musicId)

        music.acknowledgeFailure()
        val saved = musicRepository.save(music)
        log.info("Acknowledged music's failure status: musicId=${musicId.value}")

        return saved
    }
}
