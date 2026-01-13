package com.jammking.loopbox.adapter.`in`.web.dto.video

data class WebVideo(
    val id: String,
    val projectId: String,
    val status: String,
    val totalDurationSeconds: Int,
    val segments: List<WebVideoSegment>,
    val imageGroups: List<WebVideoImageGroup>
) { companion object }
