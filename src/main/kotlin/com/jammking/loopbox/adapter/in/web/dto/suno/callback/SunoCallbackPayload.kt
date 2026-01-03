package com.jammking.loopbox.adapter.`in`.web.dto.suno.callback

data class SunoCallbackPayload(
    val code: Int,
    val msg: String,
    val data: SunoCallbackData?
)
