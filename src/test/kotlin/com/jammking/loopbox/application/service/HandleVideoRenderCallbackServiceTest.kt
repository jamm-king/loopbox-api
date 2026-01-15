package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.HandleVideoRenderCallbackUseCase
import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoStatus
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class HandleVideoRenderCallbackServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var videoRepository: VideoRepository

    @Mock
    private lateinit var videoFileRepository: VideoFileRepository

    @InjectMocks
    private lateinit var handleVideoRenderCallbackService: HandleVideoRenderCallbackService

    @Test
    fun `handle should complete render`() {
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = UserId("user-1"), title = "Project")
        val video = Video(projectId = projectId)
        val savedFile = VideoFile(id = VideoFileId("file-1"), path = "video.mp4")
        video.startRender()

        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(videoRepository.findByProjectId(projectId)).thenReturn(video)
        whenever(videoRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(videoFileRepository.save(any())).thenReturn(savedFile)

        handleVideoRenderCallbackService.handle(
            HandleVideoRenderCallbackUseCase.Command(
                projectId = projectId,
                status = HandleVideoRenderCallbackUseCase.Status.COMPLETED,
                outputPath = "video.mp4"
            )
        )

        assertEquals(VideoStatus.READY, video.status)
        assertEquals(savedFile.id, video.fileId)
    }
}
