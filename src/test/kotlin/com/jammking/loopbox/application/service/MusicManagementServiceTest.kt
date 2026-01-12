package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.out.MusicAiClient
import com.jammking.loopbox.application.port.out.MusicAiRouter
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicOperation
import com.jammking.loopbox.domain.entity.music.MusicStatus
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.project.ProjectStatus
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class MusicManagementServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var musicRepository: MusicRepository

    @Mock
    private lateinit var versionRepository: MusicVersionRepository

    @Mock
    private lateinit var taskRepository: MusicGenerationTaskRepository

    @Mock
    private lateinit var musicAiRouter: MusicAiRouter

    @Mock
    private lateinit var musicAiClient: MusicAiClient

    @InjectMocks
    private lateinit var musicManagementService: MusicManagementService

    @Test
    fun `createMusic should save and return music`() {
        // Given
        val projectId = ProjectId("project-1")
        val alias = "My Song"
        whenever(projectRepository.findById(projectId)).thenReturn(
            Project(id = projectId, title = "Project")
        )
        whenever(musicRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = musicManagementService.createMusic(projectId, alias)

        // Then
        assertEquals(projectId, result.projectId)
        assertEquals(alias, result.alias)
        verify(musicRepository).save(any())
    }

    @Test
    fun `deleteMusic should delete music and mark project draft when empty`() {
        // Given
        val projectId = ProjectId("project-1")
        val project = Project(id = projectId, title = "Project", status = ProjectStatus.MUSIC_READY)
        val music = Music(id = MusicId("music-1"), projectId = projectId)
        val task = MusicGenerationTask(
            musicId = music.id,
            externalId = ExternalId("external-1"),
            status = MusicGenerationTaskStatus.GENERATING,
            provider = MusicAiProvider.SUNO
        )
        whenever(musicRepository.findById(music.id)).thenReturn(music)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(taskRepository.findByMusicId(music.id)).thenReturn(listOf(task))
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(musicRepository.findByProjectId(projectId)).thenReturn(emptyList())
        whenever(projectRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        musicManagementService.deleteMusic(music.id)

        // Then
        verify(musicRepository).deleteById(music.id)
        verify(versionRepository).deleteByMusicId(music.id)
        val taskCaptor = argumentCaptor<MusicGenerationTask>()
        verify(taskRepository).save(taskCaptor.capture())
        assertEquals(MusicGenerationTaskStatus.CANCELED, taskCaptor.firstValue.status)
        verify(projectRepository).save(project)
        assertEquals(ProjectStatus.DRAFT, project.status)
    }

    @Test
    fun `generateVersion should request ai and save task`() {
        // Given
        val projectId = ProjectId("project-1")
        val music = Music(projectId = projectId, status = MusicStatus.IDLE)
        val project = Project(id = projectId, title = "Project")
        val command = MusicManagementUseCase.GenerateVersionCommand(
            musicId = music.id,
            config = MusicConfig(mood = "chill"),
            provider = MusicAiProvider.SUNO
        )
        whenever(musicRepository.findById(music.id)).thenReturn(music)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(musicRepository.save(music)).thenReturn(music)
        whenever(musicAiRouter.getClient(MusicAiProvider.SUNO)).thenReturn(musicAiClient)
        whenever(musicAiClient.generate(any()))
            .thenReturn(MusicAiClient.GenerateResult(ExternalId("external-1")))
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = musicManagementService.generateVersion(command)

        // Then
        assertTrue(result.isGenerating())
        val captor = argumentCaptor<MusicGenerationTask>()
        verify(taskRepository).save(captor.capture())
        assertEquals(MusicGenerationTaskStatus.GENERATING, captor.firstValue.status)
    }

    @Test
    fun `generateVersion should mark failure and rethrow when ai fails`() {
        // Given
        val projectId = ProjectId("project-1")
        val music = Music(projectId = projectId, status = MusicStatus.IDLE)
        val project = Project(id = projectId, title = "Project")
        val command = MusicManagementUseCase.GenerateVersionCommand(
            musicId = music.id,
            config = MusicConfig(mood = "chill"),
            provider = MusicAiProvider.SUNO
        )
        whenever(musicRepository.findById(music.id)).thenReturn(music)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(musicRepository.save(music)).thenReturn(music)
        whenever(musicAiRouter.getClient(MusicAiProvider.SUNO)).thenReturn(musicAiClient)
        whenever(musicAiClient.generate(any()))
            .thenThrow(IllegalStateException("boom"))

        // When & Then
        assertThrows(IllegalStateException::class.java) {
            musicManagementService.generateVersion(command)
        }
        verify(musicRepository, times(2)).save(music)
        assertEquals(MusicStatus.FAILED, music.status)
        assertEquals(MusicOperation.GENERATE_VERSION, music.lastOperation)
    }

    @Test
    fun `deleteVersion should delete version and reset status`() {
        // Given
        val projectId = ProjectId("project-1")
        val music = Music(projectId = projectId, status = MusicStatus.IDLE)
        val version = MusicVersion(
            id = MusicVersionId("version-1"),
            musicId = music.id,
            config = MusicConfig()
        )
        whenever(musicRepository.findById(music.id)).thenReturn(music)
        whenever(versionRepository.findById(version.id)).thenReturn(version)
        whenever(musicRepository.save(music)).thenReturn(music)

        // When
        val result = musicManagementService.deleteVersion(music.id, version.id)

        // Then
        assertEquals(MusicStatus.IDLE, result.status)
        verify(versionRepository).deleteById(version.id)
    }

    @Test
    fun `acknowledgeFailure should reset status`() {
        // Given
        val projectId = ProjectId("project-1")
        val music = Music(
            projectId = projectId,
            status = MusicStatus.FAILED,
            lastOperation = MusicOperation.GENERATE_VERSION
        )
        whenever(musicRepository.findById(music.id)).thenReturn(music)
        whenever(musicRepository.save(music)).thenReturn(music)

        // When
        val result = musicManagementService.acknowledgeFailure(music.id)

        // Then
        assertEquals(MusicStatus.IDLE, result.status)
        assertNull(result.lastOperation)
    }
}
