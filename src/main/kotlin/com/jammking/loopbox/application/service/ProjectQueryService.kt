package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProjectQueryService(
    private val projectRepository: ProjectRepository
): ProjectQueryUseCase {

    private  val log = LoggerFactory.getLogger(javaClass)

    override fun getProjectDetail(projectId: ProjectId) =
        projectRepository.findById(projectId) ?: throw ProjectNotFoundException.byProjectId(projectId)

    override fun getAllProjects(): List<Project> =
        projectRepository.findAll()
}