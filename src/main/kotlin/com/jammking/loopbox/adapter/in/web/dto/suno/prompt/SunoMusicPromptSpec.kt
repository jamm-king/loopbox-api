package com.jammking.loopbox.adapter.`in`.web.dto.suno.prompt

data class SunoMusicPromptSpec(
    val title: String,
    val mood: String? = null,
    val bpm: Int? = null,
    val melody: String? = null,
    val harmony: String? = null,
    val bass: String? = null,
    val beat: String? = null
)