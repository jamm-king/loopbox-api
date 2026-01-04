package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.HandleMusicGenerationCallbackUseCase
import com.jammking.loopbox.application.port.out.MusicFileStorage
import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicStatus
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.project.ProjectStatus
import com.jammking.loopbox.domain.entity.task.ExternalId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.entity.task.MusicGenerationTask
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicGenerationTaskRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class HandleMusicGenerationCallbackServiceTest {

    @Mock
    private lateinit var projectRepository: ProjectRepository

    @Mock
    private lateinit var musicRepository: MusicRepository

    @Mock
    private lateinit var versionRepository: MusicVersionRepository

    @Mock
    private lateinit var taskRepository: MusicGenerationTaskRepository

    @Mock
    private lateinit var fileRepository: AudioFileRepository

    @Mock
    private lateinit var notificationPort: NotificationPort

    @Mock
    private lateinit var musicFileStorage: MusicFileStorage

    @InjectMocks
    private lateinit var handleMusicGenerationCallbackService: HandleMusicGenerationCallbackService

    @Test
    fun `handle should complete generation and notify`() {
        // Given
        val projectId = ProjectId("project-1")
        val musicId = com.jammking.loopbox.domain.entity.music.MusicId("music-1")
        val provider = MusicAiProvider.SUNO
        val externalId = ExternalId("external-1")
        val config = MusicConfig(mood = "chill")
        val track = HandleMusicGenerationCallbackUseCase.Command.Track(
            prompt = "prompt",
            remoteUrl = "https://example.com/file.mp3",
            durationSeconds = 120
        )
        val command = HandleMusicGenerationCallbackUseCase.Command(
            provider = provider,
            status = HandleMusicGenerationCallbackUseCase.Command.Status.COMPLETED,
            externalId = externalId,
            tracks = listOf(track),
            message = null
        )
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            status = MusicGenerationTaskStatus.GENERATING,
            provider = provider
        )
        val music = Music(
            id = musicId,
            projectId = projectId,
            status = MusicStatus.GENERATING,
            requestedConfig = config
        )
        val project = Project(id = projectId, title = "Project", status = ProjectStatus.DRAFT)

        whenever(taskRepository.findByProviderAndExternalId(provider, externalId)).thenReturn(task)
        whenever(musicRepository.findById(musicId)).thenReturn(music)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(versionRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(musicFileStorage.saveFromRemoteUrl(any(), any(), any(), any()))
            .thenReturn("local/path.mp3")
        whenever(fileRepository.save(any())).thenReturn(AudioFile(id = AudioFileId("file-1"), path = "local/path.mp3"))
        whenever(musicRepository.save(music)).thenReturn(music)
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(projectRepository.save(project)).thenReturn(project)

        // When
        handleMusicGenerationCallbackService.handle(command)

        // Then
        assertEquals(MusicStatus.IDLE, music.status)
        assertEquals(MusicGenerationTaskStatus.COMPLETED, task.status)
        assertEquals(ProjectStatus.MUSIC_READY, project.status)

        verify(notificationPort).notifyVersionGenerationCompleted(any(), any(), any())
    }

    @Test
    fun `handle should mark failure and notify`() {
        // Given
        val projectId = ProjectId("project-1")
        val musicId = com.jammking.loopbox.domain.entity.music.MusicId("music-1")
        val provider = MusicAiProvider.SUNO
        val externalId = ExternalId("external-1")
        val config = MusicConfig(mood = "sad")
        val command = HandleMusicGenerationCallbackUseCase.Command(
            provider = provider,
            status = HandleMusicGenerationCallbackUseCase.Command.Status.FAILED,
            externalId = externalId,
            tracks = null,
            message = "generation failed"
        )
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            status = MusicGenerationTaskStatus.GENERATING,
            provider = provider
        )
        val music = Music(
            id = musicId,
            projectId = projectId,
            status = MusicStatus.GENERATING,
            requestedConfig = config
        )
        val project = Project(id = projectId, title = "Project")

        whenever(taskRepository.findByProviderAndExternalId(provider, externalId)).thenReturn(task)
        whenever(musicRepository.findById(musicId)).thenReturn(music)
        whenever(projectRepository.findById(projectId)).thenReturn(project)
        whenever(versionRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(musicRepository.save(music)).thenReturn(music)
        whenever(taskRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        handleMusicGenerationCallbackService.handle(command)

        // Then
        assertEquals(MusicStatus.FAILED, music.status)
        assertEquals(MusicGenerationTaskStatus.FAILED, task.status)
        verify(notificationPort).notifyVersionGenerationFailed(projectId, musicId)

        val versionCaptor = argumentCaptor<MusicVersion>()
        verify(versionRepository).save(versionCaptor.capture())
        assertEquals(MusicVersionStatus.GENERATION_FAILED, versionCaptor.firstValue.status)
    }

    @Test
    fun `handle should ignore callback when task is canceled`() {
        // Given
        val musicId = com.jammking.loopbox.domain.entity.music.MusicId("music-1")
        val provider = MusicAiProvider.SUNO
        val externalId = ExternalId("external-1")
        val command = HandleMusicGenerationCallbackUseCase.Command(
            provider = provider,
            status = HandleMusicGenerationCallbackUseCase.Command.Status.COMPLETED,
            externalId = externalId,
            tracks = emptyList(),
            message = null
        )
        val task = MusicGenerationTask(
            musicId = musicId,
            externalId = externalId,
            status = MusicGenerationTaskStatus.CANCELED,
            provider = provider
        )

        whenever(taskRepository.findByProviderAndExternalId(provider, externalId)).thenReturn(task)

        // When
        handleMusicGenerationCallbackService.handle(command)

        // Then
        verify(musicRepository, never()).findById(any())
        verify(notificationPort, never()).notifyVersionGenerationCompleted(any(), any(), any())
        verify(notificationPort, never()).notifyVersionGenerationFailed(any(), any())
    }
}
