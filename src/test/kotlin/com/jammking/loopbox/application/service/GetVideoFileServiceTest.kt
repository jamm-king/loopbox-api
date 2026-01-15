package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.GetVideoFileUseCase
import com.jammking.loopbox.application.port.out.ResolveLocalVideoPort
import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoStatus
import com.jammking.loopbox.domain.exception.file.VideoFileNotFoundException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.exception.video.InvalidVideoStateException
import com.jammking.loopbox.domain.exception.video.VideoNotFoundException
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
class GetVideoFileServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var videoRepository: VideoRepository

    @Mock
    private lateinit var videoFileRepository: VideoFileRepository

    @Mock
    private lateinit var resolveLocalVideoPort: ResolveLocalVideoPort

    @InjectMocks
    private lateinit var getVideoFileService: GetVideoFileService

    @Test
    fun `getVideoTarget should return stream target`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val fileId = VideoFileId("file-1")
        val video = Video(projectId = projectId, status = VideoStatus.READY, fileId = fileId)
        val file = VideoFile(id = fileId, path = "video.mp4")
        val target = GetVideoFileUseCase.VideoStreamTarget(
            path = Paths.get("video.mp4"),
            contentType = "video/mp4",
            contentLength = 123L
        )

        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(videoRepository.findByProjectId(projectId)).thenReturn(video)
        `when`(videoFileRepository.findById(fileId)).thenReturn(file)
        `when`(resolveLocalVideoPort.resolve(file.path)).thenReturn(target)

        val result = getVideoFileService.getVideoTarget(userId, projectId)

        assertEquals(target, result)
    }

    @Test
    fun `getVideoTarget should throw when project missing`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("missing-project")
        `when`(projectRepository.findById(projectId)).thenReturn(null)

        assertThrows(ProjectNotFoundException::class.java) {
            getVideoFileService.getVideoTarget(userId, projectId)
        }
    }

    @Test
    fun `getVideoTarget should throw when video missing`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(videoRepository.findByProjectId(projectId)).thenReturn(null)

        assertThrows(VideoNotFoundException::class.java) {
            getVideoFileService.getVideoTarget(userId, projectId)
        }
    }

    @Test
    fun `getVideoTarget should throw when video not ready`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val video = Video(projectId = projectId, status = VideoStatus.DRAFT)
        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(videoRepository.findByProjectId(projectId)).thenReturn(video)

        assertThrows(InvalidVideoStateException::class.java) {
            getVideoFileService.getVideoTarget(userId, projectId)
        }
    }

    @Test
    fun `getVideoTarget should throw when file missing`() {
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val fileId = VideoFileId("file-1")
        val video = Video(projectId = projectId, status = VideoStatus.READY, fileId = fileId)
        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(videoRepository.findByProjectId(projectId)).thenReturn(video)
        `when`(videoFileRepository.findById(fileId)).thenReturn(null)

        assertThrows(VideoFileNotFoundException::class.java) {
            getVideoFileService.getVideoTarget(userId, projectId)
        }
    }
}
