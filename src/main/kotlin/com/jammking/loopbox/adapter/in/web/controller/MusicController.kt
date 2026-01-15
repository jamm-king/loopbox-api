package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.music.*
import com.jammking.loopbox.adapter.`in`.web.mapper.WebMusicMapper.toWeb
import com.jammking.loopbox.application.port.`in`.MusicManagementUseCase
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
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
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @RequestBody(required = false) request: CreateMusicRequest?
    ): CreateMusicResponse {
        val music = musicManagementUseCase.createMusic(UserId(userId), ProjectId(projectId), request?.alias)
        val webMusic = music.toWeb()
        return CreateMusicResponse(webMusic)
    }

    @GetMapping("/{musicId}")
    fun getMusic(
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @PathVariable musicId: String
    ): GetMusicResponse {
        val getResult = musicQueryUseCase.getMusicDetail(UserId(userId), MusicId(musicId))
        return GetMusicResponse.from(getResult)
    }

    @GetMapping
    fun getMusicList(
        @RequestParam userId: String,
        @PathVariable projectId: String
    ): GetMusicListResponse {
        val musicList = musicQueryUseCase.getMusicListForProject(UserId(userId), ProjectId(projectId))
        return GetMusicListResponse(
            musicList = musicList.map { it.toWeb() }
        )
    }

    @PatchMapping("/{musicId}")
    fun updateMusic(
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @RequestBody request: UpdateMusicRequest
    ): UpdateMusicResponse {
        val command = MusicManagementUseCase.UpdateMusicCommand(
            userId = UserId(userId),
            musicId = MusicId(musicId),
            alias = request.alias
        )
        val music = musicManagementUseCase.updateMusic(command)
        val webMusic = music.toWeb()
        return UpdateMusicResponse(webMusic)
    }

    @DeleteMapping("/{musicId}")
    fun deleteMusic(
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @PathVariable musicId: String
    ) {
        musicManagementUseCase.deleteMusic(UserId(userId), MusicId(musicId))
    }

    @PostMapping("/{musicId}/version/generate")
    fun generateVersion(
        @RequestParam userId: String,
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
            userId = UserId(userId),
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
        @RequestParam userId: String,
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @PathVariable versionId: String
    ): DeleteVersionResponse {
        val music = musicManagementUseCase.deleteVersion(UserId(userId), MusicId(musicId), MusicVersionId(versionId))
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
