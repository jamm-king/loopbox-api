package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.application.port.out.ResolveLocalAudioPort
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.exception.file.AudioFileNotFoundException
import com.jammking.loopbox.domain.exception.music.InvalidMusicVersionStateException
import com.jammking.loopbox.domain.exception.music.MusicVersionNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetMusicVersionAudioService(
    private val versionRepository: MusicVersionRepository,
    private val fileRepository: AudioFileRepository,
    private val resolveLocalAudioPort: ResolveLocalAudioPort
): GetMusicVersionAudioUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getAudioTarget(
        musicId: MusicId,
        versionId: MusicVersionId
    ): GetMusicVersionAudioUseCase.AudioStreamTarget {

        val version = versionRepository.findById(versionId)
            ?: throw MusicVersionNotFoundException.byVersionId(versionId)

        if(version.musicId != musicId)
            throw MusicVersionNotFoundException.byMusicId(musicId)

        if(!version.isReady())
            throw InvalidMusicVersionStateException(version.id, version.status, "get audio")

        val fileId = version.fileId
            ?: throw InvalidMusicVersionStateException(version.id, version.status, "get audio")

        val file = fileRepository.findById(fileId)
            ?: throw AudioFileNotFoundException.byAudioFileId(fileId)

        return resolveLocalAudioPort.resolve(file.path)
    }
}