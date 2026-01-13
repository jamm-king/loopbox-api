package com.jammking.loopbox.adapter.`in`.web.dto.video

data class WebVideoSegment(
    val id: String,
    val musicVersionId: String,
    val musicId: String,
    val durationSeconds: Int,
    val order: Int
)
