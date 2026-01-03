package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetAllProjectResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebProjectMapper.toWeb
import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/project")
class ProjectController(
    private val projectQueryUseCase: ProjectQueryUseCase,
    private val projectManagementUseCase: ProjectManagementUseCase
) {
    @PostMapping
    fun createProject(
        @RequestBody request: CreateProjectRequest
    ): CreateProjectResponse {
        val project = projectManagementUseCase.createProject(
            title = request.title,
        )
        val webProject = project.toWeb()
        return CreateProjectResponse(webProject)
    }

    @GetMapping("/{projectId}")
    fun getProject(
        @PathVariable projectId: String
    ): GetProjectResponse {
        val project = projectQueryUseCase.getProjectDetail(ProjectId(projectId))
        val webProject = project.toWeb()
        return GetProjectResponse(webProject)
    }

    @GetMapping
    fun getAllProject(): GetAllProjectResponse {
        val webProjectList = projectQueryUseCase.getAllProjects().map { it.toWeb() }
        return GetAllProjectResponse(webProjectList)
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: String
    ) {
        projectManagementUseCase.deleteProject(ProjectId(projectId))
    }
}