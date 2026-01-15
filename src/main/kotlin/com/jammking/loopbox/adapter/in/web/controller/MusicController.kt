package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.music.*
import com.jammking.loopbox.adapter.`in`.web.mapper.WebMusicMapper.toWeb
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
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
    private val musicManagementUseCase: MusicManagementUseCase,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/create")
    fun createMusic(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @RequestBody(required = false) request: CreateMusicRequest?
    ): CreateMusicResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val music = musicManagementUseCase.createMusic(userId, ProjectId(projectId), request?.alias)
        val webMusic = music.toWeb()
        return CreateMusicResponse(webMusic)
    }

    @GetMapping("/{musicId}")
    fun getMusic(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable musicId: String
    ): GetMusicResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val getResult = musicQueryUseCase.getMusicDetail(userId, MusicId(musicId))
        return GetMusicResponse.from(getResult)
    }

    @GetMapping
    fun getMusicList(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): GetMusicListResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val musicList = musicQueryUseCase.getMusicListForProject(userId, ProjectId(projectId))
        return GetMusicListResponse(
            musicList = musicList.map { it.toWeb() }
        )
    }

    @PatchMapping("/{musicId}")
    fun updateMusic(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @RequestBody request: UpdateMusicRequest
    ): UpdateMusicResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val command = MusicManagementUseCase.UpdateMusicCommand(
            userId = userId,
            musicId = MusicId(musicId),
            alias = request.alias
        )
        val music = musicManagementUseCase.updateMusic(command)
        val webMusic = music.toWeb()
        return UpdateMusicResponse(webMusic)
    }

    @DeleteMapping("/{musicId}")
    fun deleteMusic(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable musicId: String
    ) {
        val userId = authenticatedUserResolver.resolve(authorization)
        musicManagementUseCase.deleteMusic(userId, MusicId(musicId))
    }

    @PostMapping("/{musicId}/version/generate")
    fun generateVersion(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @RequestBody request: GenerateVersionRequest
    ): GenerateVersionResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val provider = try {
            MusicAiProvider.valueOf(request.provider)
        } catch(e: IllegalArgumentException) {
            throw InvalidMusicAiProvider(request.provider)
        }
        val command = MusicManagementUseCase.GenerateVersionCommand(
            userId = userId,
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
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @PathVariable musicId: String,
        @PathVariable versionId: String
    ): DeleteVersionResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val music = musicManagementUseCase.deleteVersion(userId, MusicId(musicId), MusicVersionId(versionId))
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
