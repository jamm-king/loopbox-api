package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.dto.video.UpdateVideoRequest
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.VideoManagementUseCase
import com.jammking.loopbox.application.port.`in`.VideoQueryUseCase
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import com.jammking.loopbox.domain.entity.video.VideoSegment
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(VideoController::class)
class VideoControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var videoQueryUseCase: VideoQueryUseCase

    @MockitoBean
    private lateinit var videoManagementUseCase: VideoManagementUseCase

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @MockitoBean
    private lateinit var authenticatedUserResolver: AuthenticatedUserResolver

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `getVideo should return video detail`() {
        // Given
        val userId = "user-1"
        val authorization = "Bearer token"
        val projectId = "project-1"
        val video = Video(
            projectId = ProjectId(projectId),
            segments = listOf(
                VideoSegment(
                    musicVersionId = MusicVersionId("music-version-1"),
                    musicId = MusicId("music-1"),
                    durationSeconds = 30
                )
            ),
            imageGroups = listOf(
                VideoImageGroup(
                    imageVersionId = ImageVersionId("image-version-1"),
                    imageId = ImageId("image-1"),
                    segmentIndexStart = 0,
                    segmentIndexEnd = 0
                )
            )
        )
        whenever(videoQueryUseCase.getVideoDetail(UserId(userId), ProjectId(projectId))).thenReturn(video)
        whenever(authenticatedUserResolver.resolve(anyOrNull(), anyOrNull())).thenReturn(UserId(userId))

        // When & Then
        mockMvc.perform(get("/api/project/{projectId}/video", projectId)
            .header("Authorization", authorization))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.video.projectId").value(projectId))
            .andExpect(jsonPath("$.video.segments[0].musicId").value("music-1"))
            .andExpect(jsonPath("$.video.imageGroups[0].imageId").value("image-1"))
    }

    @Test
    fun `updateVideo should update timeline`() {
        // Given
        val userId = "user-1"
        val authorization = "Bearer token"
        val projectId = "project-1"
        val request = UpdateVideoRequest(
            segments = listOf(UpdateVideoRequest.SegmentRequest("music-version-1")),
            imageGroups = listOf(UpdateVideoRequest.ImageGroupRequest("image-version-1", 0, 0))
        )
        val video = Video(
            projectId = ProjectId(projectId),
            segments = listOf(
                VideoSegment(
                    musicVersionId = MusicVersionId("music-version-1"),
                    musicId = MusicId("music-1"),
                    durationSeconds = 30
                )
            )
        )
        whenever(videoManagementUseCase.updateVideo(any())).thenReturn(video)
        whenever(authenticatedUserResolver.resolve(anyOrNull(), anyOrNull())).thenReturn(UserId(userId))

        // When & Then
        mockMvc.perform(
            put("/api/project/{projectId}/video", projectId)
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.video.segments[0].musicVersionId").value("music-version-1"))
            .andExpect(jsonPath("$.video.totalDurationSeconds").value(30))
    }

    @Test
    fun `renderVideo should return rendered video`() {
        // Given
        val userId = "user-1"
        val authorization = "Bearer token"
        val projectId = "project-1"
        val video = Video(projectId = ProjectId(projectId))
        whenever(videoManagementUseCase.requestRender(UserId(userId), ProjectId(projectId))).thenReturn(video)
        whenever(authenticatedUserResolver.resolve(anyOrNull(), anyOrNull())).thenReturn(UserId(userId))

        // When & Then
        mockMvc.perform(post("/api/project/{projectId}/video/render", projectId)
            .header("Authorization", authorization))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.video.projectId").value(projectId))
    }
}
