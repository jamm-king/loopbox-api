package com.jammking.loopbox.adapter.`in`.web.dto.suno.callback

import com.fasterxml.jackson.annotation.JsonProperty

data class SunoCallbackTrack(
    val id: String,
    @JsonProperty("audio_url")
    val audioUrl: String?,
    @JsonProperty("source_audio_url")
    val sourceAudioUrl: String?,
    @JsonProperty("stream_audio_url")
    val streamAudioUrl: String?,
    @JsonProperty("source_stream_audio_url")
    val sourceStreamAudioUrl: String?,
    @JsonProperty("image_url")
    val imageUrl: String?,
    @JsonProperty("source_image_url")
    val sourceImageUrl: String?,
    val prompt: String?,
    @JsonProperty("model_name")
    val modelName: String?,
    val title: String?,
    val tags: String?,
    val createTime: String?,
    val duration: Double?
)