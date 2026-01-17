package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.application.port.out.ResolveLocalAudioPort
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.file.AudioFileNotFoundException
import com.jammking.loopbox.domain.exception.music.InvalidMusicVersionStateException
import com.jammking.loopbox.domain.exception.music.MusicVersionNotFoundException
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectMusicInconsistentStateException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMusicVersionAudioService(
    private val versionRepository: MusicVersionRepository,
    private val musicRepository: MusicRepository,
    private val projectRepository: ProjectRepository,
    private val fileRepository: AudioFileRepository,
    private val resolveLocalAudioPort: ResolveLocalAudioPort
): GetMusicVersionAudioUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getAudioTarget(
        userId: UserId,
        musicId: MusicId,
        versionId: MusicVersionId
    ): GetMusicVersionAudioUseCase.AudioStreamTarget {

        val version = versionRepository.findById(versionId)
            ?: throw MusicVersionNotFoundException.byVersionId(versionId)

        if(version.musicId != musicId)
            throw MusicVersionNotFoundException.byMusicId(musicId)

        val music = musicRepository.findById(musicId)
            ?: throw MusicVersionNotFoundException.byMusicId(musicId)
        val project = projectRepository.findById(music.projectId)
            ?: throw ProjectMusicInconsistentStateException.projectMissingForMusic(music.id, music.projectId)
        requireOwner(project, userId)

        if(!version.isReady())
            throw InvalidMusicVersionStateException(version.id, version.status, "get audio")

        val fileId = version.fileId
            ?: throw InvalidMusicVersionStateException(version.id, version.status, "get audio")

        val file = fileRepository.findById(fileId)
            ?: throw AudioFileNotFoundException.byAudioFileId(fileId)

        return resolveLocalAudioPort.resolve(file.path)
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
