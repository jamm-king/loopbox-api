package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.music.MusicNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
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

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @InjectMocks
    private lateinit var musicQueryService: MusicQueryService

    @Test
    fun `getMusicDetail should return music and versions`() {
        // Given
        val userId = UserId("user-1")
        val musicId = MusicId("music-1")
        val projectId = ProjectId("project-1")
        val music = Music(id = musicId, projectId = projectId)
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val versions = listOf(
            MusicVersion(musicId = musicId, config = MusicConfig(mood = "chill")),
            MusicVersion(musicId = musicId, config = MusicConfig(mood = "energetic"))
        )
        `when`(musicRepository.findById(musicId)).thenReturn(music)
        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(versionRepository.findByMusicId(musicId)).thenReturn(versions)

        // When
        val result = musicQueryService.getMusicDetail(userId, musicId)

        // Then
        assertEquals(MusicQueryUseCase.GetMusicDetailResult(music, versions), result)
        verify(musicRepository).findById(musicId)
        verify(versionRepository).findByMusicId(musicId)
    }

    @Test
    fun `getMusicDetail should throw exception when music not found`() {
        // Given
        val userId = UserId("user-1")
        val musicId = MusicId("missing-music")
        `when`(musicRepository.findById(musicId)).thenReturn(null)

        // When & Then
        assertThrows(MusicNotFoundException::class.java) {
            musicQueryService.getMusicDetail(userId, musicId)
        }
    }

    @Test
    fun `getMusicListForProject should return list`() {
        // Given
        val userId = UserId("user-1")
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, ownerUserId = userId, title = "Project")
        val musicList = listOf(
            Music(id = MusicId("music-1"), projectId = projectId),
            Music(id = MusicId("music-2"), projectId = projectId)
        )
        `when`(projectRepository.findById(projectId)).thenReturn(project)
        `when`(musicRepository.findByProjectId(projectId)).thenReturn(musicList)

        // When
        val result = musicQueryService.getMusicListForProject(userId, projectId)

        // Then
        assertEquals(musicList, result)
        verify(musicRepository).findByProjectId(projectId)
    }
}
