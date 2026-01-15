package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.support.AudioStreamResponder
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@RestController
@RequestMapping("/api/music/{musicId}/versions/{versionId}")
class MusicVersionAudioController(
    private val getMusicVersionAudioUseCase: GetMusicVersionAudioUseCase,
    private val audioStreamResponder: AudioStreamResponder,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/audio")
    fun streamAudio(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestParam(required = false) accessToken: String?,
        @PathVariable musicId: String,
        @PathVariable versionId: String,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<StreamingResponseBody> {

        val userId = authenticatedUserResolver.resolve(authorization, accessToken)
        val target = getMusicVersionAudioUseCase.getAudioTarget(
            userId = userId,
            musicId = MusicId(musicId),
            versionId = MusicVersionId(versionId)
        )

        return audioStreamResponder.respond(target, headers)
    }
}
