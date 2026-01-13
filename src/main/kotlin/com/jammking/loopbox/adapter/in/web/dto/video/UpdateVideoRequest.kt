package com.jammking.loopbox.adapter.`in`.web.dto.video

data class UpdateVideoRequest(
    val segments: List<SegmentRequest>,
    val imageGroups: List<ImageGroupRequest>
) {
    data class SegmentRequest(
        val musicVersionId: String
    )

    data class ImageGroupRequest(
        val imageVersionId: String,
        val segmentIndexStart: Int,
        val segmentIndexEnd: Int
    )
}
