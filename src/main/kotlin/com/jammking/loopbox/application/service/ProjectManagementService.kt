package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
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
    private val versionRepository: MusicVersionRepository,
    private val taskRepository: MusicGenerationTaskRepository
): ProjectManagementUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun createProject(title: String): Project {
        val project = Project(title = title)
        val saved = projectRepository.save(project)
        log.info("Created project: projectId=${project.id.value}, title=${project.title}")

        return saved
    }

    override fun deleteProject(projectId: ProjectId) {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        val musics = musicRepository.findByProjectId(projectId)

        musics.forEach { music ->
            musicRepository.deleteById(music.id)
            versionRepository.deleteByMusicId(music.id)
            taskRepository.deleteByMusicId(music.id)
        }
        projectRepository.deleteById(projectId)
        log.info("Deleted project: projectId=${projectId.value}")
    }

    override fun renameTitle(projectId: ProjectId, newTitle: String) {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)

        val before = project.title
        project.rename(newTitle)
        val saved = projectRepository.save(project)
        log.info("Renamed project title: $before -> ${saved.title}")
    }
}