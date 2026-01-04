package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.application.port.out.ResolveLocalAudioPort
import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import com.jammking.loopbox.domain.exception.file.AudioFileNotFoundException
import com.jammking.loopbox.domain.exception.music.InvalidMusicVersionStateException
import com.jammking.loopbox.domain.exception.music.MusicVersionNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
class GetMusicVersionAudioServiceTest {

    @Mock
    private lateinit var versionRepository: MusicVersionRepository

    @Mock
    private lateinit var fileRepository: AudioFileRepository

    @Mock
    private lateinit var resolveLocalAudioPort: ResolveLocalAudioPort

    @InjectMocks
    private lateinit var getMusicVersionAudioService: GetMusicVersionAudioService

    @Test
    fun `getAudioTarget should return audio stream target`() {
        // Given
        val musicId = MusicId("music-1")
        val versionId = MusicVersionId("version-1")
        val fileId = AudioFileId("file-1")
        val version = MusicVersion(
            id = versionId,
            musicId = musicId,
            status = MusicVersionStatus.READY,
            config = MusicConfig(),
            fileId = fileId
        )
        val audioFile = AudioFile(id = fileId, path = "local/path.mp3")
        val target = GetMusicVersionAudioUseCase.AudioStreamTarget(
            path = Paths.get("local/path.mp3"),
            contentType = "audio/mpeg",
            contentLength = 123L
        )
        `when`(versionRepository.findById(versionId)).thenReturn(version)
        `when`(fileRepository.findById(fileId)).thenReturn(audioFile)
        `when`(resolveLocalAudioPort.resolve(audioFile.path)).thenReturn(target)

        // When
        val result = getMusicVersionAudioService.getAudioTarget(musicId, versionId)

        // Then
        assertEquals(target, result)
    }

    @Test
    fun `getAudioTarget should throw when version not found`() {
        // Given
        val versionId = MusicVersionId("missing-version")
        `when`(versionRepository.findById(versionId)).thenReturn(null)

        // When & Then
        assertThrows(MusicVersionNotFoundException::class.java) {
            getMusicVersionAudioService.getAudioTarget(MusicId("music-1"), versionId)
        }
    }

    @Test
    fun `getAudioTarget should throw when music id mismatched`() {
        // Given
        val requestedMusicId = MusicId("music-1")
        val versionId = MusicVersionId("version-1")
        val version = MusicVersion(
            id = versionId,
            musicId = MusicId("other-music"),
            status = MusicVersionStatus.READY,
            config = MusicConfig(),
            fileId = AudioFileId("file-1")
        )
        `when`(versionRepository.findById(versionId)).thenReturn(version)

        // When & Then
        assertThrows(MusicVersionNotFoundException::class.java) {
            getMusicVersionAudioService.getAudioTarget(requestedMusicId, versionId)
        }
    }

    @Test
    fun `getAudioTarget should throw when version not ready`() {
        // Given
        val musicId = MusicId("music-1")
        val versionId = MusicVersionId("version-1")
        val version = MusicVersion(
            id = versionId,
            musicId = musicId,
            status = MusicVersionStatus.GENERATED,
            config = MusicConfig()
        )
        `when`(versionRepository.findById(versionId)).thenReturn(version)

        // When & Then
        assertThrows(InvalidMusicVersionStateException::class.java) {
            getMusicVersionAudioService.getAudioTarget(musicId, versionId)
        }
    }

    @Test
    fun `getAudioTarget should throw when file missing`() {
        // Given
        val musicId = MusicId("music-1")
        val versionId = MusicVersionId("version-1")
        val fileId = AudioFileId("file-1")
        val version = MusicVersion(
            id = versionId,
            musicId = musicId,
            status = MusicVersionStatus.READY,
            config = MusicConfig(),
            fileId = fileId
        )
        `when`(versionRepository.findById(versionId)).thenReturn(version)
        `when`(fileRepository.findById(fileId)).thenReturn(null)

        // When & Then
        assertThrows(AudioFileNotFoundException::class.java) {
            getMusicVersionAudioService.getAudioTarget(musicId, versionId)
        }
    }
}
