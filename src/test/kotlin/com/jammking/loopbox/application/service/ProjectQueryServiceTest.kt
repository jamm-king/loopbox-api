package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ProjectQueryServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @InjectMocks
    private lateinit var projectQueryService: ProjectQueryService

    @Test
    fun `getProjectDetail should return project`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Test Project")
        `when`(projectRepository.findById(projectId)).thenReturn(project)

        // When
        val result = projectQueryService.getProjectDetail(userId, projectId)

        // Then
        assertEquals(project, result)
        verify(projectRepository).findById(projectId)
    }

    @Test
    fun `getProjectDetail should throw exception when project not found`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("missing-project")
        `when`(projectRepository.findById(projectId)).thenReturn(null)

        // When & Then
        assertThrows(ProjectNotFoundException::class.java) {
            projectQueryService.getProjectDetail(userId, projectId)
        }
    }

    @Test
    fun `getAllProjects should return list`() {
        // Given
        val userId = UserId("user-1")
        val projects = listOf(
            Project(id = ProjectId("project-1"), ownerUserId = userId, title = "Project 1"),
            Project(id = ProjectId("project-2"), ownerUserId = UserId("user-2"), title = "Project 2")
        )
        `when`(projectRepository.findAll()).thenReturn(projects)

        // When
        val result = projectQueryService.getAllProjects(userId)

        // Then
        assertEquals(listOf(projects.first()), result)
        verify(projectRepository).findAll()
    }
}
