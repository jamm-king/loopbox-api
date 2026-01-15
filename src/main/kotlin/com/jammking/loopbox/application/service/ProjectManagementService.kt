package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProjectManagementService(
    private val projectRepository: ProjectRepository,
    private val musicRepository: MusicRepository,
    private val musicVersionRepository: MusicVersionRepository,
    private val musicTaskRepository: MusicGenerationTaskRepository,
    private val imageRepository: ImageRepository,
    private val imageVersionRepository: ImageVersionRepository,
    private val imageTaskRepository: ImageGenerationTaskRepository
): ProjectManagementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun createProject(userId: UserId, title: String): Project {
        val project = Project(ownerUserId = userId, title = title)
        val saved = projectRepository.save(project)
        log.info("Created project: projectId=${project.id.value}, title=${project.title}")

        return saved
    }

    override fun deleteProject(userId: UserId, projectId: ProjectId) {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)
        val musics = musicRepository.findByProjectId(projectId)
        val images = imageRepository.findByProjectId(projectId)

        musics.forEach { music ->
            val tasks = musicTaskRepository.findByMusicId(music.id)
            tasks.forEach { task ->
                task.markCanceled()
                musicTaskRepository.save(task)
            }
            musicRepository.deleteById(music.id)
            musicVersionRepository.deleteByMusicId(music.id)
        }

        images.forEach { image ->
            val tasks = imageTaskRepository.findByImageId(image.id)
            tasks.forEach { task ->
                task.markCanceled()
                imageTaskRepository.save(task)
            }
            imageRepository.deleteById(image.id)
            imageVersionRepository.deleteByImageId(image.id)
        }
        projectRepository.deleteById(projectId)
        log.info("Deleted project: projectId=${projectId.value}")
    }

    override fun renameTitle(userId: UserId, projectId: ProjectId, newTitle: String) {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)

        val before = project.title
        project.rename(newTitle)
        val saved = projectRepository.save(project)
        log.info("Renamed project title: $before -> ${saved.title}")
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
