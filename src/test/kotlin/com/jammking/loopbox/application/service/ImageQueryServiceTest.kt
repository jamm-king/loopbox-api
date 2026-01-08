package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.application.port.out.ResolveImageAccessPort
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ImageQueryServiceTest {

    @Mock
    private lateinit var imageRepository: ImageRepository

    @Mock
    private lateinit var versionRepository: ImageVersionRepository

    @Mock
    private lateinit var fileRepository: ImageFileRepository

    @Mock
    private lateinit var imageAccessResolver: ResolveImageAccessPort

    @InjectMocks
    private lateinit var imageQueryService: ImageQueryService

    @Test
    fun `getImageDetail should return image and versions`() {
        val imageId = ImageId("image-1")
        val image = Image(id = imageId, projectId = ProjectId("project-1"))
        val fileId = ImageFileId("file-1")
        val versions = listOf(
            ImageVersion(imageId = imageId, config = ImageConfig(description = "A"), fileId = fileId),
            ImageVersion(imageId = imageId, config = ImageConfig(description = "B"))
        )

        whenever(imageRepository.findById(imageId)).thenReturn(image)
        whenever(versionRepository.findByImageId(imageId)).thenReturn(versions)
        whenever(fileRepository.findById(fileId)).thenReturn(ImageFile(id = fileId, path = "/tmp/image.png"))
        whenever(imageAccessResolver.resolve("/tmp/image.png"))
            .thenReturn(ResolveImageAccessPort.ImageAccessTarget(url = "http://localhost/static/image/1.png"))

        val result = imageQueryService.getImageDetail(imageId)

        assertEquals(image, result.image)
        assertEquals(versions, result.versions)
        assertEquals(
            mapOf(versions.first().id to "http://localhost/static/image/1.png"),
            result.versionUrls
        )
    }

    @Test
    fun `getImageListForProject should return images`() {
        val projectId = ProjectId("project-1")
        val images = listOf(
            Image(id = ImageId("image-1"), projectId = projectId),
            Image(id = ImageId("image-2"), projectId = projectId)
        )

        whenever(imageRepository.findByProjectId(projectId)).thenReturn(images)

        val result = imageQueryService.getImageListForProject(projectId)

        assertEquals(images, result)
    }
}
