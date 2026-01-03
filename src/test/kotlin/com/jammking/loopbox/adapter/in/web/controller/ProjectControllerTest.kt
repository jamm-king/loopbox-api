package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.project.CreateProjectRequest
import com.jammking.loopbox.application.port.`in`.ProjectManagementUseCase
import com.jammking.loopbox.application.port.`in`.ProjectQueryUseCase
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
        val title = "New Project"
        val request = CreateProjectRequest(title = title)
        val project = Project(title = title)
        given(projectManagementUseCase.createProject(title)).willReturn(project)

        // When & Then
        mockMvc.perform(post("/api/project")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.project.title").value(title))
    }

    @Test
    fun `getProject should return project detail`() {
        // Given
        val projectId = "project-1"
        val project = Project(id = ProjectId(projectId), title = "Test Project")
        given(projectQueryUseCase.getProjectDetail(ProjectId(projectId))).willReturn(project)

        // When & Then
        mockMvc.perform(get("/api/project/{projectId}", projectId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.project.id").value(projectId))
            .andExpect(jsonPath("$.project.title").value("Test Project"))
    }

    @Test
    fun `getAllProject should return list of projects`() {
        // Given
        val project1 = Project(id = ProjectId("p1"), title = "Project 1")
        val project2 = Project(id = ProjectId("p2"), title = "Project 2")
        given(projectQueryUseCase.getAllProjects()).willReturn(listOf(project1, project2))

        // When & Then
        mockMvc.perform(get("/api/project"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.projectList[0].id").value("p1"))
            .andExpect(jsonPath("$.projectList[1].id").value("p2"))
    }

    @Test
    fun `deleteProject should call delete usecase`() {
        // Given
        val projectId = "project-1"

        // When & Then
        mockMvc.perform(delete("/api/project/{projectId}", projectId))
            .andExpect(status().isOk)

        verify(projectManagementUseCase).deleteProject(ProjectId(projectId))
    }
}
