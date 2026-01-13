package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.video.WebVideo
import com.jammking.loopbox.adapter.`in`.web.dto.video.WebVideoImageGroup
import com.jammking.loopbox.adapter.`in`.web.dto.video.WebVideoSegment
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import com.jammking.loopbox.domain.entity.video.VideoSegment

object WebVideoMapper {

    fun Video.toWeb() =
        WebVideo(
            id = id.value,
            projectId = projectId.value,
            status = status.name,
            totalDurationSeconds = totalDurationSeconds(),
            segments = segments.mapIndexed { index, segment -> segment.toWeb(index) },
            imageGroups = imageGroups.map { it.toWeb() }
        )

    private fun VideoSegment.toWeb(order: Int) =
        WebVideoSegment(
            id = id.value,
            musicVersionId = musicVersionId.value,
            musicId = musicId.value,
            durationSeconds = durationSeconds,
            order = order
        )

    private fun VideoImageGroup.toWeb() =
        WebVideoImageGroup(
            id = id.value,
            imageVersionId = imageVersionId.value,
            imageId = imageId.value,
            segmentIndexStart = segmentIndexStart,
            segmentIndexEnd = segmentIndexEnd
        )
}
