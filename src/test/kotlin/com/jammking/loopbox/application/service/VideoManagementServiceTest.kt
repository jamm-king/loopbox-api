package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.VideoManagementUseCase
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoStatus
import com.jammking.loopbox.domain.exception.video.InvalidVideoEditException
import com.jammking.loopbox.domain.exception.video.InvalidVideoStateException
import com.jammking.loopbox.application.port.out.VideoFileStorage
import com.jammking.loopbox.application.port.out.VideoRenderClient
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class VideoManagementServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var videoRepository: VideoRepository

    @Mock
    private lateinit var musicVersionRepository: MusicVersionRepository

    @Mock
    private lateinit var imageVersionRepository: ImageVersionRepository

    @Mock
    private lateinit var audioFileRepository: AudioFileRepository

    @Mock
    private lateinit var imageFileRepository: ImageFileRepository

    @Mock
    private lateinit var videoFileStorage: VideoFileStorage

    @Mock
    private lateinit var videoRenderClient: VideoRenderClient

    @InjectMocks
    private lateinit var videoManagementService: VideoManagementService

    @Test
    fun `updateVideo should create timeline`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))

        val musicVersionId = MusicVersionId("music-version-1")
        val musicVersion = MusicVersion(
            id = musicVersionId,
            musicId = MusicId("music-1"),
            config = com.jammking.loopbox.domain.entity.music.MusicConfig(),
            durationSeconds = 30
        )
        whenever(musicVersionRepository.findById(musicVersionId)).thenReturn(musicVersion)

        val imageVersionId = ImageVersionId("image-version-1")
        val imageVersion = ImageVersion(
            id = imageVersionId,
            imageId = ImageId("image-1"),
            config = com.jammking.loopbox.domain.entity.image.ImageConfig()
        )
        whenever(imageVersionRepository.findById(imageVersionId)).thenReturn(imageVersion)

        whenever(videoRepository.findByProjectId(projectId)).thenReturn(null)
        whenever(videoRepository.save(any())).thenAnswer { it.arguments[0] }

        val command = VideoManagementUseCase.UpdateVideoCommand(
            userId = userId,
            projectId = projectId,
            segments = listOf(VideoManagementUseCase.SegmentInput(musicVersionId)),
            imageGroups = listOf(
                VideoManagementUseCase.ImageGroupInput(
                    imageVersionId = imageVersionId,
                    segmentIndexStart = 0,
                    segmentIndexEnd = 0
                )
            )
        )

        // When
        val result = videoManagementService.updateVideo(command)

        // Then
        assertEquals(1, result.segments.size)
        assertEquals(1, result.imageGroups.size)
        assertEquals(VideoStatus.DRAFT, result.status)
    }

    @Test
    fun `updateVideo should reject overlapping image groups`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))

        val musicVersionId = MusicVersionId("music-version-1")
        val musicVersion = MusicVersion(
            id = musicVersionId,
            musicId = MusicId("music-1"),
            config = com.jammking.loopbox.domain.entity.music.MusicConfig(),
            durationSeconds = 30
        )
        whenever(musicVersionRepository.findById(musicVersionId)).thenReturn(musicVersion)

        val imageVersionId = ImageVersionId("image-version-1")
        val imageVersion = ImageVersion(
            id = imageVersionId,
            imageId = ImageId("image-1"),
            config = com.jammking.loopbox.domain.entity.image.ImageConfig()
        )
        whenever(imageVersionRepository.findById(imageVersionId)).thenReturn(imageVersion)

        val command = VideoManagementUseCase.UpdateVideoCommand(
            userId = userId,
            projectId = projectId,
            segments = listOf(
                VideoManagementUseCase.SegmentInput(musicVersionId),
                VideoManagementUseCase.SegmentInput(musicVersionId)
            ),
            imageGroups = listOf(
                VideoManagementUseCase.ImageGroupInput(
                    imageVersionId = imageVersionId,
                    segmentIndexStart = 0,
                    segmentIndexEnd = 1
                ),
                VideoManagementUseCase.ImageGroupInput(
                    imageVersionId = imageVersionId,
                    segmentIndexStart = 1,
                    segmentIndexEnd = 1
                )
            )
        )

        // When & Then
        assertThrows(InvalidVideoEditException::class.java) {
            videoManagementService.updateVideo(command)
        }
    }

    @Test
    fun `requestRender should start rendering`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val video = Video(projectId = projectId)
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))
        whenever(videoRepository.findByProjectId(projectId)).thenReturn(video)
        whenever(videoRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(videoFileStorage.prepareRenderPath(projectId, video.id)).thenReturn("video.mp4")

        // When
        val result = videoManagementService.requestRender(userId, projectId)

        // Then
        assertEquals(VideoStatus.RENDERING, result.status)
        verify(videoRenderClient).requestRender(any())
    }

    @Test
    fun `requestRender should not persist rendering when render command validation fails`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val musicVersionId = MusicVersionId("music-version-1")
        val musicId = MusicId("music-1")
        val video = Video(
            projectId = projectId,
            segments = listOf(
                com.jammking.loopbox.domain.entity.video.VideoSegment(
                    musicVersionId = musicVersionId,
                    musicId = musicId,
                    durationSeconds = 10
                )
            )
        )
        val musicVersion = MusicVersion(
            id = musicVersionId,
            musicId = musicId,
            config = com.jammking.loopbox.domain.entity.music.MusicConfig(),
            durationSeconds = 10
        )
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))
        whenever(videoRepository.findByProjectId(projectId)).thenReturn(video)
        whenever(musicVersionRepository.findById(musicVersionId)).thenReturn(musicVersion)

        // When & Then
        assertThrows(InvalidVideoEditException::class.java) {
            videoManagementService.requestRender(userId, projectId)
        }
        assertEquals(VideoStatus.DRAFT, video.status)
        verify(videoRepository, never()).save(any())
        verify(videoRenderClient, never()).requestRender(any())
    }

    @Test
    fun `requestRender should reject when already rendering`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val video = Video(projectId = projectId)
        video.startRender()
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))
        whenever(videoRepository.findByProjectId(projectId)).thenReturn(video)
        whenever(videoFileStorage.prepareRenderPath(projectId, video.id)).thenReturn("video.mp4")

        // When & Then
        assertThrows(InvalidVideoStateException::class.java) {
            videoManagementService.requestRender(userId, projectId)
        }
        assertEquals(VideoStatus.RENDERING, video.status)
        verify(videoRepository, never()).save(any())
        verify(videoRenderClient, never()).requestRender(any())
    }

    @Test
    fun `requestRender should keep draft when output path preparation fails`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val video = Video(projectId = projectId)
        whenever(projectRepository.findById(projectId)).thenReturn(Project(id = projectId, ownerUserId = userId, title = "Project"))
        whenever(videoRepository.findByProjectId(projectId)).thenReturn(video)
        whenever(videoFileStorage.prepareRenderPath(projectId, video.id))
            .thenThrow(IllegalStateException("render path error"))

        // When & Then
        assertThrows(IllegalStateException::class.java) {
            videoManagementService.requestRender(userId, projectId)
        }
        assertEquals(VideoStatus.DRAFT, video.status)
        verify(videoRepository, never()).save(any())
        verify(videoRenderClient, never()).requestRender(any())
    }
}
