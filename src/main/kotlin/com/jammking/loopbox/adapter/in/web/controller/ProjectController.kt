package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetAllProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.UpdateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.UpdateProjectResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebProjectMapper.toWeb
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/project")
class ProjectController(
    private val projectQueryUseCase: ProjectQueryUseCase,
    private val projectManagementUseCase: ProjectManagementUseCase,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {
    @PostMapping
    fun createProject(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestBody request: CreateProjectRequest
    ): CreateProjectResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val project = projectManagementUseCase.createProject(
            userId = userId,
            title = request.title
        )
        val webProject = project.toWeb()
        return CreateProjectResponse(webProject)
    }

    @GetMapping("/{projectId}")
    fun getProject(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): GetProjectResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val project = projectQueryUseCase.getProjectDetail(userId, ProjectId(projectId))
        val webProject = project.toWeb()
        return GetProjectResponse(webProject)
    }

    @GetMapping
    fun getAllProject(
        @RequestHeader("Authorization", required = false) authorization: String?
    ): GetAllProjectResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val webProjectList = projectQueryUseCase.getAllProjects(userId).map { it.toWeb() }
        return GetAllProjectResponse(webProjectList)
    }

    @PatchMapping("/{projectId}")
    fun updateProject(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @RequestBody request: UpdateProjectRequest
    ): UpdateProjectResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        projectManagementUseCase.renameTitle(userId, ProjectId(projectId), request.title)
        val project = projectQueryUseCase.getProjectDetail(userId, ProjectId(projectId))
        return UpdateProjectResponse(project.toWeb())
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ) {
        val userId = authenticatedUserResolver.resolve(authorization)
        projectManagementUseCase.deleteProject(userId, ProjectId(projectId))
    }
}
