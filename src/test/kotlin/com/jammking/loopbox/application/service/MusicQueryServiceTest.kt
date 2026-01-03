package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.music.MusicNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MusicQueryServiceTest {

    @Mock
    private lateinit var musicRepository: MusicRepository

    @Mock
    private lateinit var versionRepository: MusicVersionRepository

    @Mock
    private lateinit var fileRepository: AudioFileRepository

    @InjectMocks
    private lateinit var musicQueryService: MusicQueryService

    @Test
    fun `getMusicDetail should return music and versions`() {
        // Given
        val musicId = MusicId("music-1")
        val music = Music(id = musicId, projectId = ProjectId("project-1"))
        val versions = listOf(
            MusicVersion(musicId = musicId, config = MusicConfig(mood = "chill")),
            MusicVersion(musicId = musicId, config = MusicConfig(mood = "energetic"))
        )
        `when`(musicRepository.findById(musicId)).thenReturn(music)
        `when`(versionRepository.findByMusicId(musicId)).thenReturn(versions)

        // When
        val result = musicQueryService.getMusicDetail(musicId)

        // Then
        assertEquals(MusicQueryUseCase.GetMusicDetailResult(music, versions), result)
        verify(musicRepository).findById(musicId)
        verify(versionRepository).findByMusicId(musicId)
    }

    @Test
    fun `getMusicDetail should throw exception when music not found`() {
        // Given
        val musicId = MusicId("missing-music")
        `when`(musicRepository.findById(musicId)).thenReturn(null)

        // When & Then
        assertThrows(MusicNotFoundException::class.java) {
            musicQueryService.getMusicDetail(musicId)
        }
    }

    @Test
    fun `getMusicListForProject should return list`() {
        // Given
        val projectId = ProjectId("project-1")
        val musicList = listOf(
            Music(id = MusicId("music-1"), projectId = projectId),
            Music(id = MusicId("music-2"), projectId = projectId)
        )
        `when`(musicRepository.findByProjectId(projectId)).thenReturn(musicList)

        // When
        val result = musicQueryService.getMusicListForProject(projectId)

        // Then
        assertEquals(musicList, result)
        verify(musicRepository).findByProjectId(projectId)
    }
}
