package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.video.GetVideoResponse
import com.jammking.loopbox.adapter.`in`.web.dto.video.RenderVideoResponse
import com.jammking.loopbox.adapter.`in`.web.dto.video.UpdateVideoRequest
import com.jammking.loopbox.adapter.`in`.web.dto.video.UpdateVideoResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebVideoMapper.toWeb
import com.jammking.loopbox.adapter.`in`.web.support.AuthenticatedUserResolver
import com.jammking.loopbox.application.port.`in`.VideoManagementUseCase
import com.jammking.loopbox.application.port.`in`.VideoQueryUseCase
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.user.UserId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/project/{projectId}/video")
class VideoController(
    private val videoQueryUseCase: VideoQueryUseCase,
    private val videoManagementUseCase: VideoManagementUseCase,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    @GetMapping
    fun getVideo(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): GetVideoResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val video = videoQueryUseCase.getVideoDetail(userId, ProjectId(projectId))
        return GetVideoResponse(video.toWeb())
    }

    @PutMapping
    fun updateVideo(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String,
        @RequestBody request: UpdateVideoRequest
    ): UpdateVideoResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val command = VideoManagementUseCase.UpdateVideoCommand(
            userId = userId,
            projectId = ProjectId(projectId),
            segments = request.segments.map {
                VideoManagementUseCase.SegmentInput(
                    musicVersionId = MusicVersionId(it.musicVersionId)
                )
            },
            imageGroups = request.imageGroups.map {
                VideoManagementUseCase.ImageGroupInput(
                    imageVersionId = ImageVersionId(it.imageVersionId),
                    segmentIndexStart = it.segmentIndexStart,
                    segmentIndexEnd = it.segmentIndexEnd
                )
            }
        )
        val video = videoManagementUseCase.updateVideo(command)
        return UpdateVideoResponse(video.toWeb())
    }

    @PostMapping("/render")
    fun renderVideo(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable projectId: String
    ): RenderVideoResponse {
        val userId = authenticatedUserResolver.resolve(authorization)
        val video = videoManagementUseCase.requestRender(userId, ProjectId(projectId))
        return RenderVideoResponse(video.toWeb())
    }
}
