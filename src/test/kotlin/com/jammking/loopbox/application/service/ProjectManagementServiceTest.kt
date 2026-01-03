package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProjectManagementServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var musicRepository: MusicRepository

    @Mock
    private lateinit var versionRepository: MusicVersionRepository

    @Mock
    private lateinit var taskRepository: MusicGenerationTaskRepository

    @InjectMocks
    private lateinit var projectManagementService: ProjectManagementService

    @Test
    fun `createProject should save and return project`() {
        // Given
        val title = "Test Project"
        val project = Project(title = title)
        whenever(projectRepository.save(any())).thenReturn(project)

        // When
        val result = projectManagementService.createProject(title)

        // Then
        assertEquals(title, result.title)
        verify(projectRepository).save(any())
    }

    @Test
    fun `deleteProject should delete project and related data`() {
        // Given
        val projectId = ProjectId(UUID.randomUUID().toString())
        val project = Project(id = projectId, title = "Test Project")
        val musicId = MusicId("music-1")
        
        val music = mock<Music>()
        whenever(music.id).thenReturn(musicId)
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(musicRepository.findByProjectId(projectId)).thenReturn(listOf(music))

        // When
        projectManagementService.deleteProject(projectId)

        // Then
        verify(musicRepository).deleteById(musicId)
        verify(versionRepository).deleteByMusicId(musicId)
        verify(taskRepository).deleteByMusicId(musicId)
        verify(projectRepository).deleteById(projectId)
    }

    @Test
    fun `deleteProject should throw exception when project not found`() {
        // Given
        val projectId = ProjectId("non-existent-id")
        whenever(projectRepository.findById(projectId)).thenReturn(null)

        // When & Then
        assertThrows(ProjectNotFoundException::class.java) {
            projectManagementService.deleteProject(projectId)
        }
    }

    @Test
    fun `renameTitle should update project title`() {
        // Given
        val projectId = ProjectId("project-1")
        val oldTitle = "Old Title"
        val newTitle = "New Title"
        val project = Project(id = projectId, title = oldTitle)
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(projectRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        projectManagementService.renameTitle(projectId, newTitle)

        // Then
        assertEquals(newTitle, project.title)
        verify(projectRepository).save(project)
    }

}
