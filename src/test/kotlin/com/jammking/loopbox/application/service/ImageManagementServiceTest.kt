package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.ImageManagementUseCase
import com.jammking.loopbox.application.port.out.ImageAiClient
import com.jammking.loopbox.application.port.out.ImageAiRouter
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageOperation
import com.jammking.loopbox.domain.entity.image.ImageStatus
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.entity.task.ImageGenerationTask
import com.jammking.loopbox.domain.entity.task.ImageGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.ImageGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ImageManagementServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var imageRepository: ImageRepository

    @Mock
    private lateinit var versionRepository: ImageVersionRepository

    @Mock
    private lateinit var taskRepository: ImageGenerationTaskRepository

    @Mock
    private lateinit var imageAiRouter: ImageAiRouter

    @Mock
    private lateinit var imageAiClient: ImageAiClient

    @InjectMocks
    private lateinit var imageManagementService: ImageManagementService

    @Test
    fun `createImage should save and return image`() {
        // Given
        val projectId = ProjectId("project-1")
        whenever(projectRepository.findById(projectId)).thenReturn(
            Project(id = projectId, title = "Project")
        )
        whenever(imageRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = imageManagementService.createImage(projectId)

        // Then
        assertEquals(projectId, result.projectId)
        verify(imageRepository).save(any())
    }

    @Test
    fun `deleteImage should delete image and related tasks`() {
        // Given
        val projectId = ProjectId("project-1")
        val image = Image(id = ImageId("image-1"), projectId = projectId)
        val task = ImageGenerationTask(
            imageId = image.id,
            externalId = ExternalId("external-1"),
            status = ImageGenerationTaskStatus.GENERATING,
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        whenever(imageRepository.findById(image.id)).thenReturn(image)
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, title = "Project"))
        whenever(taskRepository.findByImageId(image.id)).thenReturn(listOf(task))
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        imageManagementService.deleteImage(image.id)

        // Then
        verify(imageRepository).deleteById(image.id)
        verify(versionRepository).deleteByImageId(image.id)
        val taskCaptor = argumentCaptor<ImageGenerationTask>()
        verify(taskRepository).save(taskCaptor.capture())
        assertEquals(ImageGenerationTaskStatus.CANCELED, taskCaptor.firstValue.status)
    }

    @Test
    fun `generateVersion should request ai and save task`() {
        // Given
        val projectId = ProjectId("project-1")
        val image = Image(projectId = projectId, status = ImageStatus.IDLE)
        val project = Project(id = projectId, title = "Project")
        val command = ImageManagementUseCase.GenerateVersionCommand(
            imageId = image.id,
            config = ImageConfig(description = "Sunrise"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        whenever(imageRepository.findById(image.id)).thenReturn(image)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(imageRepository.save(image)).thenReturn(image)
        whenever(imageAiRouter.getClient(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4)).thenReturn(imageAiClient)
        whenever(imageAiClient.generate(any()))
            .thenReturn(ImageAiClient.GenerateResult(ExternalId("external-1")))
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = imageManagementService.generateVersion(command)

        // Then
        assertTrue(result.isGenerating())
        val captor = argumentCaptor<ImageGenerationTask>()
        verify(taskRepository).save(captor.capture())
        assertEquals(ImageGenerationTaskStatus.GENERATING, captor.firstValue.status)
    }

    @Test
    fun `generateVersion should mark failure and rethrow when ai fails`() {
        // Given
        val projectId = ProjectId("project-1")
        val image = Image(projectId = projectId, status = ImageStatus.IDLE)
        val project = Project(id = projectId, title = "Project")
        val command = ImageManagementUseCase.GenerateVersionCommand(
            imageId = image.id,
            config = ImageConfig(description = "Sunrise"),
            provider = ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4
        )
        whenever(imageRepository.findById(image.id)).thenReturn(image)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(imageRepository.save(image)).thenReturn(image)
        whenever(imageAiRouter.getClient(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4)).thenReturn(imageAiClient)
        whenever(imageAiClient.generate(any()))
            .thenThrow(IllegalStateException("boom"))

        // When & Then
        assertThrows(IllegalStateException::class.java) {
            imageManagementService.generateVersion(command)
        }
        verify(imageRepository, times(2)).save(image)
        assertEquals(ImageStatus.FAILED, image.status)
        assertEquals(ImageOperation.GENERATE_VERSION, image.lastOperation)
    }

    @Test
    fun `deleteVersion should delete version and reset status`() {
        // Given
        val projectId = ProjectId("project-1")
        val image = Image(projectId = projectId, status = ImageStatus.IDLE)
        val version = ImageVersion(
            id = ImageVersionId("version-1"),
            imageId = image.id,
            config = ImageConfig()
        )
        whenever(imageRepository.findById(image.id)).thenReturn(image)
        whenever(versionRepository.findById(version.id)).thenReturn(version)
        whenever(imageRepository.save(image)).thenReturn(image)

        // When
        val result = imageManagementService.deleteVersion(image.id, version.id)

        // Then
        assertEquals(ImageStatus.IDLE, result.status)
        verify(versionRepository).deleteById(version.id)
    }

    @Test
    fun `acknowledgeFailure should reset status`() {
        // Given
        val projectId = ProjectId("project-1")
        val image = Image(
            projectId = projectId,
            status = ImageStatus.FAILED,
            lastOperation = ImageOperation.GENERATE_VERSION
        )
        whenever(imageRepository.findById(image.id)).thenReturn(image)
        whenever(imageRepository.save(image)).thenReturn(image)

        // When
        val result = imageManagementService.acknowledgeFailure(image.id)

        // Then
        assertEquals(ImageStatus.IDLE, result.status)
        assertNull(result.lastOperation)
    }
}
