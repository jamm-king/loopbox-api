package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectRequest
import com.jammking.loopbox.adapter.`in`.web.dto.project.UpdateProjectRequest
import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory

@WebMvcTest(ProjectController::class)
class ProjectControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var projectQueryUseCase: ProjectQueryUseCase

    @MockitoBean
    private lateinit var projectManagementUseCase: ProjectManagementUseCase

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createProject should return created project`() {
        // Given
        val userId = "user-1"
        val title = "New Project"
        val request = CreateProjectRequest(title = title)
        val project = Project(ownerUserId = UserId(userId), title = title)
        whenever(projectManagementUseCase.createProject(UserId(userId), title)).thenReturn(project)

        // When & Then
        mockMvc.perform(post("/api/project")
            .param("userId", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.project.title").value(title))
    }

    @Test
    fun `getProject should return project detail`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val project = Project(id = ProjectId(projectId), ownerUserId = UserId(userId), title = "Test Project")
        whenever(projectQueryUseCase.getProjectDetail(UserId(userId), ProjectId(projectId))).thenReturn(project)

        // When & Then
        mockMvc.perform(get("/api/project/{projectId}", projectId)
            .param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.project.id").value(projectId))
            .andExpect(jsonPath("$.project.title").value("Test Project"))
    }

    @Test
    fun `getAllProject should return list of projects`() {
        // Given
        val userId = "user-1"
        val project1 = Project(id = ProjectId("p1"), ownerUserId = UserId(userId), title = "Project 1")
        val project2 = Project(id = ProjectId("p2"), ownerUserId = UserId("user-2"), title = "Project 2")
        whenever(projectQueryUseCase.getAllProjects(UserId(userId))).thenReturn(listOf(project1))

        // When & Then
        mockMvc.perform(get("/api/project")
            .param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.projectList[0].id").value("p1"))
    }

    @Test
    fun `updateProject should update title`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val request = UpdateProjectRequest(title = "Updated Project")
        val project = Project(id = ProjectId(projectId), ownerUserId = UserId(userId), title = request.title)
        whenever(projectQueryUseCase.getProjectDetail(UserId(userId), ProjectId(projectId))).thenReturn(project)

        // When & Then
        mockMvc.perform(
            patch("/api/project/{projectId}", projectId)
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.project.id").value(projectId))
            .andExpect(jsonPath("$.project.title").value("Updated Project"))

        verify(projectManagementUseCase).renameTitle(UserId(userId), ProjectId(projectId), request.title)
    }

    @Test
    fun `deleteProject should call delete usecase`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"

        // When & Then
        mockMvc.perform(delete("/api/project/{projectId}", projectId)
            .param("userId", userId))
            .andExpect(status().isOk)

        verify(projectManagementUseCase).deleteProject(UserId(userId), ProjectId(projectId))
    }
}
