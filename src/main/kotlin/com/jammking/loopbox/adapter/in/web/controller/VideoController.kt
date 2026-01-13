package com.jammking.loopbox.adapter.`in`.web.controller

import com.jammking.loopbox.adapter.`in`.web.dto.video.GetVideoResponse
import com.jammking.loopbox.adapter.`in`.web.dto.video.RenderVideoResponse
import com.jammking.loopbox.adapter.`in`.web.dto.video.UpdateVideoRequest
import com.jammking.loopbox.adapter.`in`.web.dto.video.UpdateVideoResponse
import com.jammking.loopbox.adapter.`in`.web.mapper.WebVideoMapper.toWeb
import com.jammking.loopbox.application.port.`in`.VideoManagementUseCase
import com.jammking.loopbox.application.port.`in`.VideoQueryUseCase
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/project/{projectId}/video")
class VideoController(
    private val videoQueryUseCase: VideoQueryUseCase,
    private val videoManagementUseCase: VideoManagementUseCase
) {

    @GetMapping
    fun getVideo(
        @PathVariable projectId: String
    ): GetVideoResponse {
        val video = videoQueryUseCase.getVideoDetail(ProjectId(projectId))
        return GetVideoResponse(video.toWeb())
    }

    @PutMapping
    fun updateVideo(
        @PathVariable projectId: String,
        @RequestBody request: UpdateVideoRequest
    ): UpdateVideoResponse {
        val command = VideoManagementUseCase.UpdateVideoCommand(
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
        @PathVariable projectId: String
    ): RenderVideoResponse {
        val video = videoManagementUseCase.requestRender(ProjectId(projectId))
        return RenderVideoResponse(video.toWeb())
    }
}
