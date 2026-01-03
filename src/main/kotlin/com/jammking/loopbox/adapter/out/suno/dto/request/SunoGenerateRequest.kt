package com.jammking.loopbox.adapter.out.suno.dto.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SunoGenerateRequest(
    val prompt: String? = null,
    val style: String? = null,
    val title: String? = null,
    val customMode: Boolean,
    val instrumental: Boolean,
    val personaId: String? = null,
    val model: String,
    val negativeTags: String? = null,
    val vocalGender: String? = null,
    val styleWeight: Double? = null,
    val weirdnessConstraint: Double? = null,
    val audioWeight: Double? = null,
    val callBackUrl: String
)
