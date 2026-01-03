package com.jammking.loopbox.application.service

import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase.GetMusicDetailResult
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.exception.music.MusicNotFoundException
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import com.jammking.loopbox.domain.port.out.MusicRepository
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MusicQueryService(
    private val musicRepository: MusicRepository,
    private val versionRepository: MusicVersionRepository,
    private val fileRepository: AudioFileRepository
): MusicQueryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getMusicDetail(musicId: MusicId): GetMusicDetailResult {
        val music = musicRepository.findById(musicId) ?: throw MusicNotFoundException.byMusicId(musicId)
        val versions = versionRepository.findByMusicId(musicId)

        return GetMusicDetailResult(music,versions)
    }


    override fun getMusicListForProject(projectId: ProjectId): List<Music> =
        musicRepository.findByProjectId(projectId)
}