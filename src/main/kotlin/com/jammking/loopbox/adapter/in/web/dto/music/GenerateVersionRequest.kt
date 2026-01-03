package com.jammking.loopbox.adapter.`in`.web.dto.music

data class GenerateVersionRequest(
    val provider: String,
    val mood: String? = null,
    val bpm: Int? = null,
    val melody: String? = null,
    val harmony: String? = null,
    val bass: String? = null,
    val beat: String? = null
)
