package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.music.*
import com.jammking.loopbox.adapter.`in`.web.mapper.WebMusicMapper.toWeb
import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.task.MusicAiProvider
import com.jammking.loopbox.domain.exception.task.InvalidMusicAiProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/project/{projectId}/music")
class MusicController(
    private val musicQueryUseCase: MusicQueryUseCase,
    private val musicManagementUseCase: MusicManagementUseCase
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/create")
    fun createMusic(
        @PathVariable projectId: String
    ): CreateMusicResponse {
        val music = musicManagementUseCase.createMusic(ProjectId(projectId))
        val webMusic = music.toWeb()
        return CreateMusicResponse(webMusic)
    }

    @GetMapping("/{musicId}")
    fun getMusic(
        @PathVariable musicId: String
    ): GetMusicResponse {
        val getResult = musicQueryUseCase.getMusicDetail(MusicId(musicId))
        return GetMusicResponse.from(getResult)
    }

    @GetMapping
    fun getMusicList(
        @PathVariable projectId: String
    ): GetMusicListResponse {
        val musicList = musicQueryUseCase.getMusicListForProject(ProjectId(projectId))
        return GetMusicListResponse(
            musicList = musicList.map { it.toWeb() }
        )
    }

    @DeleteMapping("/{musicId}")
    fun deleteMusic(
        @PathVariable musicId: String
    ) {
        musicManagementUseCase.deleteMusic(MusicId(musicId))
    }

    @PostMapping("/{musicId}/version/generate")
    fun generateVersion(
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @RequestBody request: GenerateVersionRequest
    ): GenerateVersionResponse {
        val provider = try {
            MusicAiProvider.valueOf(request.provider)
        } catch(e: IllegalArgumentException) {
            throw InvalidMusicAiProvider(request.provider)
        }
        val command = MusicManagementUseCase.GenerateVersionCommand(
            musicId = MusicId(musicId),
            config = request.toMusicConfig(),
            provider = provider
        )
        val music = musicManagementUseCase.generateVersion(command)
        val webMusic = music.toWeb()
        return GenerateVersionResponse(webMusic)
    }

    @DeleteMapping("/{musicId}/version/{versionId}")
    fun deleteVersion(
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @PathVariable versionId: String
    ): DeleteVersionResponse {
        val music = musicManagementUseCase.deleteVersion(MusicId(musicId), MusicVersionId(versionId))
        val webMusic = music.toWeb()
        return DeleteVersionResponse(webMusic)
    }

    private fun GenerateVersionRequest.toMusicConfig() =
        MusicConfig(
            mood = mood,
            bpm = bpm,
            melody = melody,
            harmony = harmony,
            bass = bass,
            beat = beat
        )

    private fun GetMusicResponse.Companion.from(
        result: MusicQueryUseCase.GetMusicDetailResult
    ): GetMusicResponse =
        GetMusicResponse(
            music = result.music.toWeb(),
            versions = result.versions.map { it.toWeb() }
        )
}