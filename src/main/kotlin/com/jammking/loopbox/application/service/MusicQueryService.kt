package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase.GetMusicDetailResult
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.exception.music.MusicNotFoundException
import com.jammking.loopbox.domain.exception.project.InvalidProjectOwnerException
import com.jammking.loopbox.domain.exception.project.ProjectNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import com.jammking.loopbox.domain.port.out.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MusicQueryService(
    private val musicRepository: MusicRepository,
    private val versionRepository: MusicVersionRepository,
    private val fileRepository: AudioFileRepository,
    private val projectRepository: ProjectRepository
): MusicQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getMusicDetail(userId: UserId, musicId: MusicId): GetMusicDetailResult {
        val music = musicRepository.findById(musicId) ?: throw MusicNotFoundException.byMusicId(musicId)
        val project = projectRepository.findById(music.projectId)
            ?: throw ProjectNotFoundException.byProjectId(music.projectId)
        requireOwner(project, userId)
        val versions = versionRepository.findByMusicId(musicId)

        return GetMusicDetailResult(music,versions)
    }


    override fun getMusicListForProject(userId: UserId, projectId: ProjectId): List<Music> {
        val project = projectRepository.findById(projectId)
            ?: throw ProjectNotFoundException.byProjectId(projectId)
        requireOwner(project, userId)
        return musicRepository.findByProjectId(projectId)
    }

    private fun requireOwner(project: Project, userId: UserId) {
        if (project.ownerUserId != userId) {
            throw InvalidProjectOwnerException(project.id, userId, project.ownerUserId)
        }
    }
}
