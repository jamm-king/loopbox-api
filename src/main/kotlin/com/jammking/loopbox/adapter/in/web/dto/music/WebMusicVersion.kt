package com.jammking.loopbox.adapter.`in`.web.dto.music

data class WebMusicVersion(
    val id: String,
    val fileId: String?,
    val config: WebMusicConfig,
    val durationSeconds: Int?
) { companion object }
