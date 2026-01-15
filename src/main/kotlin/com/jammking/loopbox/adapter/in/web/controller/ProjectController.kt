package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.GetAllProjectResponse
import com.jammking.loopbox.adapter.`in`.web.dto.project.UpdateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.UpdateProjectResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebProjectMapper.toWeb
import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/project")
class ProjectController(
    private val projectQueryUseCase: ProjectQueryUseCase,
    private val projectManagementUseCase: ProjectManagementUseCase
) {
    @PostMapping
    fun createProject(
        @RequestParam userId: String,
        @RequestBody request: CreateProjectRequest
    ): CreateProjectResponse {
        val project = projectManagementUseCase.createProject(
            userId = UserId(userId),
            title = request.title
        )
        val webProject = project.toWeb()
        return CreateProjectResponse(webProject)
    }

    @GetMapping("/{projectId}")
    fun getProject(
        @RequestParam userId: String,
        @PathVariable projectId: String
    ): GetProjectResponse {
        val project = projectQueryUseCase.getProjectDetail(UserId(userId), ProjectId(projectId))
        val webProject = project.toWeb()
        return GetProjectResponse(webProject)
    }

    @GetMapping
    fun getAllProject(
        @RequestParam userId: String
    ): GetAllProjectResponse {
        val webProjectList = projectQueryUseCase.getAllProjects(UserId(userId)).map { it.toWeb() }
        return GetAllProjectResponse(webProjectList)
    }

    @PatchMapping("/{projectId}")
    fun updateProject(
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @RequestBody request: UpdateProjectRequest
    ): UpdateProjectResponse {
        projectManagementUseCase.renameTitle(UserId(userId), ProjectId(projectId), request.title)
        val project = projectQueryUseCase.getProjectDetail(UserId(userId), ProjectId(projectId))
        return UpdateProjectResponse(project.toWeb())
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @RequestParam userId: String,
        @PathVariable projectId: String
    ) {
        projectManagementUseCase.deleteProject(UserId(userId), ProjectId(projectId))
    }
}
