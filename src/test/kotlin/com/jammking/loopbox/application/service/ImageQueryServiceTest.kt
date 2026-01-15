package com.jammking.loopbox.application.service

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.application.port.out.ResolveImageAccessPort
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
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

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @InjectMocks
    private lateinit var imageQueryService: ImageQueryService

    @Test
    fun `getImageDetail should return image and versions`() {
        val userId = UserId("user-1")
        val imageId = ImageId("image-1")
        val projectId = ProjectId("project-1")
        val image = Image(id = imageId, projectId = projectId)
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val fileId = ImageFileId("file-1")
        val versions = listOf(
            ImageVersion(imageId = imageId, config = ImageConfig(description = "A"), fileId = fileId),
            ImageVersion(imageId = imageId, config = ImageConfig(description = "B"))
        )

        whenever(imageRepository.findById(imageId)).thenReturn(image)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(versionRepository.findByImageId(imageId)).thenReturn(versions)
        whenever(fileRepository.findById(fileId)).thenReturn(ImageFile(id = fileId, path = "/tmp/image.png"))
        whenever(imageAccessResolver.resolve("/tmp/image.png"))
            .thenReturn(ResolveImageAccessPort.ImageAccessTarget(url = "http://localhost/static/image/1.png"))

        val result = imageQueryService.getImageDetail(userId, imageId)

        assertEquals(image, result.image)
        assertEquals(versions, result.versions)
        assertEquals(
            mapOf(versions.first().id to "http://localhost/static/image/1.png"),
            result.versionUrls
        )
    }

    @Test
    fun `getImageListForProject should return images`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val images = listOf(
            Image(id = ImageId("image-1"), projectId = projectId),
            Image(id = ImageId("image-2"), projectId = projectId)
        )

        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(imageRepository.findByProjectId(projectId)).thenReturn(images)

        val result = imageQueryService.getImageListForProject(userId, projectId)

        assertEquals(images, result)
    }
}
