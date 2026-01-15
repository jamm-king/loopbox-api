package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.support.VideoStreamResponder
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.GetVideoFileUseCase
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.nio.file.Path

@WebMvcTest(VideoFileController::class)
class VideoFileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var getVideoFileUseCase: GetVideoFileUseCase

    @MockitoBean
    private lateinit var videoStreamResponder: VideoStreamResponder

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @MockitoBean
    private lateinit var authenticatedUserResolver: AuthenticatedUserResolver

    @Test
    fun `streamVideo should return response from responder`() {
        val userId = "user-1"
        val accessToken = "access-token"
        val projectId = "project-1"
        val target = GetVideoFileUseCase.VideoStreamTarget(
            path = Path.of("dummy"),
            contentType = "video/mp4",
            contentLength = 123L
        )
        val responseBody = StreamingResponseBody { }
        val responseEntity = ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(responseBody)

        whenever(getVideoFileUseCase.getVideoTarget(UserId(userId), ProjectId(projectId)))
            .thenReturn(target)
        whenever(videoStreamResponder.respond(eq(target), any()))
            .thenReturn(responseEntity)
        whenever(authenticatedUserResolver.resolve(anyOrNull(), anyOrNull())).thenReturn(UserId(userId))

        mockMvc.perform(get("/api/project/{projectId}/video/file", projectId)
            .param("accessToken", accessToken))
            .andExpect(status().isOk)

        verify(getVideoFileUseCase).getVideoTarget(UserId(userId), ProjectId(projectId))
        verify(videoStreamResponder).respond(eq(target), any())
    }
}
