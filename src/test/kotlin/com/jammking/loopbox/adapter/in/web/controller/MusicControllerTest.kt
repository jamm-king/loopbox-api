package com.jammking.loopbox.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.jammking.loopbox.adapter.`in`.web.dto.error.ErrorResponseFactory
import com.jammking.loopbox.adapter.`in`.web.dto.music.CreateMusicRequest
import com.jammking.loopbox.adapter.`in`.web.dto.music.GenerateVersionRequest
import com.jammking.loopbox.adapter.`in`.web.dto.music.UpdateMusicRequest
import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MusicController::class)
class MusicControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var musicQueryUseCase: MusicQueryUseCase

    @MockitoBean
    private lateinit var musicManagementUseCase: MusicManagementUseCase

    @MockitoBean
    private lateinit var errorResponseFactory: ErrorResponseFactory

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createMusic should return created music`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val request = CreateMusicRequest(alias = "My Song")
        val music = Music(id = MusicId("music-1"), projectId = ProjectId(projectId), alias = request.alias)
        whenever(musicManagementUseCase.createMusic(UserId(userId), ProjectId(projectId), request.alias)).thenReturn(music)

        // When & Then
        mockMvc.perform(
            post("/api/project/{projectId}/music/create", projectId)
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.music.id").value("music-1"))
            .andExpect(jsonPath("$.music.alias").value("My Song"))
            .andExpect(jsonPath("$.music.status").value("IDLE"))
    }

    @Test
    fun `getMusic should return music detail`() {
        // Given
        val userId = "user-1"
        val musicId = "music-1"
        val projectId = "project-1"
        val music = Music(id = MusicId(musicId), projectId = ProjectId(projectId), alias = "Track 1")
        val config = MusicConfig(mood = "calm", bpm = 120, melody = "soft")
        val version = MusicVersion(
            id = MusicVersionId("v1"),
            musicId = MusicId(musicId),
            config = config,
            durationSeconds = 30
        )
        val result = MusicQueryUseCase.GetMusicDetailResult(music, listOf(version))
        whenever(musicQueryUseCase.getMusicDetail(UserId(userId), MusicId(musicId))).thenReturn(result)

        // When & Then
        mockMvc.perform(get("/api/project/{projectId}/music/{musicId}", projectId, musicId)
            .param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.music.id").value(musicId))
            .andExpect(jsonPath("$.music.alias").value("Track 1"))
            .andExpect(jsonPath("$.versions[0].id").value("v1"))
            .andExpect(jsonPath("$.versions[0].config.mood").value("calm"))
            .andExpect(jsonPath("$.versions[0].config.bpm").value(120))
    }

    @Test
    fun `getMusicList should return list of music`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val music1 = Music(id = MusicId("m1"), projectId = ProjectId(projectId))
        val music2 = Music(id = MusicId("m2"), projectId = ProjectId(projectId))
        whenever(musicQueryUseCase.getMusicListForProject(UserId(userId), ProjectId(projectId))).thenReturn(listOf(music1, music2))

        // When & Then
        mockMvc.perform(get("/api/project/{projectId}/music", projectId)
            .param("userId", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.musicList[0].id").value("m1"))
            .andExpect(jsonPath("$.musicList[1].id").value("m2"))
    }

    @Test
    fun `updateMusic should update alias`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val musicId = "music-1"
        val request = UpdateMusicRequest(alias = "Updated Name")
        val music = Music(id = MusicId(musicId), projectId = ProjectId(projectId), alias = request.alias)
        whenever(musicManagementUseCase.updateMusic(any())).thenReturn(music)

        // When & Then
        mockMvc.perform(
            patch("/api/project/{projectId}/music/{musicId}", projectId, musicId)
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.music.id").value(musicId))
            .andExpect(jsonPath("$.music.alias").value("Updated Name"))
    }

    @Test
    fun `deleteMusic should call delete usecase`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val musicId = "music-1"

        // When & Then
        mockMvc.perform(delete("/api/project/{projectId}/music/{musicId}", projectId, musicId)
            .param("userId", userId))
            .andExpect(status().isOk)

        verify(musicManagementUseCase).deleteMusic(UserId(userId), MusicId(musicId))
    }

    @Test
    fun `generateVersion should call usecase and return music`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val musicId = "music-1"
        val request = GenerateVersionRequest(
            provider = "SUNO",
            mood = "calm",
            bpm = 120,
            melody = "soft"
        )
        val music = Music(id = MusicId(musicId), projectId = ProjectId(projectId))
        whenever(musicManagementUseCase.generateVersion(any())).thenReturn(music)

        // When & Then
        mockMvc.perform(
            post("/api/project/{projectId}/music/{musicId}/version/generate", projectId, musicId)
                .param("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.music.id").value(musicId))

        val captor = argumentCaptor<MusicManagementUseCase.GenerateVersionCommand>()
        verify(musicManagementUseCase).generateVersion(captor.capture())
        val command = captor.firstValue
        assertEquals(UserId(userId), command.userId)
        assertEquals(MusicAiProvider.SUNO, command.provider)
        assertEquals("calm", command.config.mood)
        assertEquals(120, command.config.bpm)
        assertEquals("soft", command.config.melody)
    }

    @Test
    fun `deleteVersion should call delete usecase`() {
        // Given
        val userId = "user-1"
        val projectId = "project-1"
        val musicId = "music-1"
        val versionId = "v1"
        val music = Music(id = MusicId(musicId), projectId = ProjectId(projectId))
        whenever(musicManagementUseCase.deleteVersion(UserId(userId), MusicId(musicId), MusicVersionId(versionId)))
            .thenReturn(music)

        // When & Then
        mockMvc.perform(
            delete("/api/project/{projectId}/music/{musicId}/version/{versionId}", projectId, musicId, versionId)
                .param("userId", userId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.music.id").value(musicId))

        verify(musicManagementUseCase).deleteVersion(UserId(userId), MusicId(musicId), MusicVersionId(versionId))
    }
}
