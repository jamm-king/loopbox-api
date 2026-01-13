package com.jammking.loopbox.adapter.`in`.web.dto.video

data class WebVideoImageGroup(
    val id: String,
    val imageVersionId: String,
    val imageId: String,
    val segmentIndexStart: Int,
    val segmentIndexEnd: Int
)
