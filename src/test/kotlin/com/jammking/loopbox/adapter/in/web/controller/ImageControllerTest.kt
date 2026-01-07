package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.dto.image.GenerateImageVersionRequest
import com.jammking.loopbox.application.port.`in`.ImageManagementUseCase
import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ImageController::class)
class ImageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var imageQueryUseCase: ImageQueryUseCase

    @MockitoBean
    private lateinit var imageManagementUseCase: ImageManagementUseCase

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createImage should return created image`() {
        val projectId = "project-1"
        val image = Image(id = ImageId("image-1"), projectId = ProjectId(projectId))
        whenever(imageManagementUseCase.createImage(ProjectId(projectId))).thenReturn(image)

        mockMvc.perform(post("/api/project/{projectId}/image/create", projectId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.image.id").value("image-1"))
            .andExpect(jsonPath("$.image.status").value("IDLE"))
    }

    @Test
    fun `getImage should return image detail`() {
        val projectId = "project-1"
        val imageId = "image-1"
        val image = Image(id = ImageId(imageId), projectId = ProjectId(projectId))
        val config = ImageConfig(description = "Sunrise", width = 1024, height = 1024)
        val version = ImageVersion(
            id = ImageVersionId("v1"),
            imageId = ImageId(imageId),
            config = config
        )
        val result = ImageQueryUseCase.GetImageDetailResult(image, listOf(version))
        whenever(imageQueryUseCase.getImageDetail(ImageId(imageId))).thenReturn(result)

        mockMvc.perform(get("/api/project/{projectId}/image/{imageId}", projectId, imageId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.image.id").value(imageId))
            .andExpect(jsonPath("$.versions[0].id").value("v1"))
            .andExpect(jsonPath("$.versions[0].config.description").value("Sunrise"))
            .andExpect(jsonPath("$.versions[0].config.width").value(1024))
    }

    @Test
    fun `getImageList should return list of images`() {
        val projectId = "project-1"
        val image1 = Image(id = ImageId("i1"), projectId = ProjectId(projectId))
        val image2 = Image(id = ImageId("i2"), projectId = ProjectId(projectId))
        whenever(imageQueryUseCase.getImageListForProject(ProjectId(projectId))).thenReturn(listOf(image1, image2))

        mockMvc.perform(get("/api/project/{projectId}/image", projectId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.images[0].id").value("i1"))
            .andExpect(jsonPath("$.images[1].id").value("i2"))
    }

    @Test
    fun `deleteImage should call delete usecase`() {
        val projectId = "project-1"
        val imageId = "image-1"

        mockMvc.perform(delete("/api/project/{projectId}/image/{imageId}", projectId, imageId))
            .andExpect(status().isOk)

        verify(imageManagementUseCase).deleteImage(ImageId(imageId))
    }

    @Test
    fun `generateVersion should call usecase and return image`() {
        val projectId = "project-1"
        val imageId = "image-1"
        val request = GenerateImageVersionRequest(
            provider = "REPLICATE_GOOGLE_IMAGEN_4",
            description = "Sunset",
            width = 1600,
            height = 900
        )
        val image = Image(id = ImageId(imageId), projectId = ProjectId(projectId))
        whenever(imageManagementUseCase.generateVersion(any())).thenReturn(image)

        mockMvc.perform(
            post("/api/project/{projectId}/image/{imageId}/version/generate", projectId, imageId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.image.id").value(imageId))

        val captor = argumentCaptor<ImageManagementUseCase.GenerateVersionCommand>()
        verify(imageManagementUseCase).generateVersion(captor.capture())
        val command = captor.firstValue
        assertEquals(ImageAiProvider.REPLICATE_GOOGLE_IMAGEN_4, command.provider)
        assertEquals("Sunset", command.config.description)
        assertEquals(1600, command.config.width)
    }

    @Test
    fun `deleteVersion should call delete usecase`() {
        val projectId = "project-1"
        val imageId = "image-1"
        val versionId = "v1"
        val image = Image(id = ImageId(imageId), projectId = ProjectId(projectId))
        whenever(imageManagementUseCase.deleteVersion(ImageId(imageId), ImageVersionId(versionId)))
            .thenReturn(image)

        mockMvc.perform(
            delete("/api/project/{projectId}/image/{imageId}/version/{versionId}", projectId, imageId, versionId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.image.id").value(imageId))

        verify(imageManagementUseCase).deleteVersion(ImageId(imageId), ImageVersionId(versionId))
    }
}
