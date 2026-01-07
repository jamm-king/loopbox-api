package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
    private lateinit var musicVersionRepository: MusicVersionRepository

    @Mock
    private lateinit var musicTaskRepository: MusicGenerationTaskRepository

    @Mock
    private lateinit var imageRepository: ImageRepository

    @Mock
    private lateinit var imageVersionRepository: ImageVersionRepository

    @Mock
    private lateinit var imageTaskRepository: ImageGenerationTaskRepository

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
        val imageId = ImageId("image-1")
        
        val music = Music(id = musicId, projectId = projectId)
        val image = Image(id = imageId, projectId = projectId)
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = ExternalId("external-1"),
            status = MusicGenerationTaskStatus.GENERATING,
            provider = MusicAiProvider.SUNO
        )
        val imageTask = ImageGenerationTask(
            imageId = imageId,
            externalId = ExternalId("external-image-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(musicRepository.findByProjectId(projectId)).thenReturn(listOf(music))
        whenever(musicTaskRepository.findByMusicId(musicId)).thenReturn(listOf(task))
        whenever(musicTaskRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(imageRepository.findByProjectId(projectId)).thenReturn(listOf(image))
        whenever(imageTaskRepository.findByImageId(imageId)).thenReturn(listOf(imageTask))
        whenever(imageTaskRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        projectManagementService.deleteProject(projectId)

        // Then
        verify(musicRepository).deleteById(musicId)
        verify(musicVersionRepository).deleteByMusicId(musicId)
        val taskCaptor = argumentCaptor<MusicGenerationTask>()
        verify(musicTaskRepository).save(taskCaptor.capture())
        assertEquals(MusicGenerationTaskStatus.CANCELED, taskCaptor.firstValue.status)
        verify(imageRepository).deleteById(imageId)
        verify(imageVersionRepository).deleteByImageId(imageId)
        val imageTaskCaptor = argumentCaptor<ImageGenerationTask>()
        verify(imageTaskRepository).save(imageTaskCaptor.capture())
        assertEquals(ImageGenerationTaskStatus.CANCELED, imageTaskCaptor.firstValue.status)
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
