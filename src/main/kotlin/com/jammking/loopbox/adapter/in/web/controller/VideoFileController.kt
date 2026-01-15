package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.support.VideoStreamResponder
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.GetVideoFileUseCase
import com.jammking.loopbox.domain.entity.project.ProjectId
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
@RequestMapping("/api/project/{projectId}/video")
class VideoFileController(
    private val getVideoFileUseCase: GetVideoFileUseCase,
    private val videoStreamResponder: VideoStreamResponder,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    @GetMapping("/file")
    fun streamVideo(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestParam(required = false) accessToken: String?,
        @PathVariable projectId: String,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<StreamingResponseBody> {
        val userId = authenticatedUserResolver.resolve(authorization, accessToken)
        val target = getVideoFileUseCase.getVideoTarget(userId, ProjectId(projectId))
        return videoStreamResponder.respond(target, headers)
    }
}
