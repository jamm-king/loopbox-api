package com.jammking.loopbox.adapter.`in`.web.dto.suno.callback

import com.fasterxml.jackson.annotation.JsonProperty

data class SunoCallbackData(
    val callbackType: String,
    @JsonProperty("task_id")
    val taskId: String,
    val data: List<SunoCallbackTrack>?
)