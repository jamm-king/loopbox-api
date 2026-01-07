package com.jammking.loopbox.adapter.out.replicate.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class ReplicatePredictionRequest(
    val input: ReplicateImageInput,
    val webhook: String? = null,
    @JsonProperty("webhook_events_filter")
    val webhookEventsFilter: List<String>? = null
)

data class ReplicateImageInput(
    val prompt: String,
    @JsonProperty("aspect_ratio")
    val aspectRatio: String,
    @JsonProperty("output_format")
    val outputFormat: String,
    @JsonProperty("safety_filter_level")
    val safetyFilterLevel: String
)
