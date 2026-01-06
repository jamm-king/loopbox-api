package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.support.AudioStreamResponder
import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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

@WebMvcTest(MusicVersionAudioController::class)
class MusicVersionAudioControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var getMusicVersionAudioUseCase: GetMusicVersionAudioUseCase

    @MockitoBean
    private lateinit var audioStreamResponder: AudioStreamResponder

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Test
    fun `streamAudio should return response from responder`() {
        // Given
        val musicId = "music-1"
        val versionId = "v1"
        val target = GetMusicVersionAudioUseCase.AudioStreamTarget(
            path = Path.of("dummy"),
            contentType = "audio/mpeg",
            contentLength = 123L
        )
        val responseBody = StreamingResponseBody { }
        val responseEntity = ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(responseBody)

        whenever(getMusicVersionAudioUseCase.getAudioTarget(MusicId(musicId), MusicVersionId(versionId)))
            .thenReturn(target)
        whenever(audioStreamResponder.respond(eq(target), any()))
            .thenReturn(responseEntity)

        // When & Then
        mockMvc.perform(get("/api/music/{musicId}/versions/{versionId}/audio", musicId, versionId))
            .andExpect(status().isOk)

        verify(getMusicVersionAudioUseCase).getAudioTarget(MusicId(musicId), MusicVersionId(versionId))
        verify(audioStreamResponder).respond(eq(target), any())
    }
}
