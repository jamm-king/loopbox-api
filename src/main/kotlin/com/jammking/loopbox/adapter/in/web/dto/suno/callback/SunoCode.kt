package com.jammking.loopbox.adapter.`in`.web.dto.suno.callback

enum class SunoCode(val value: Int) {
    SUCCESS(200),
    BAD_REQUEST(400),
    DOWNLOAD_FAILED(451),
    SERVER_ERROR(500);

    companion object {
        fun from(raw: Int): SunoCode? = entries.firstOrNull { it.value == raw }
    }
}