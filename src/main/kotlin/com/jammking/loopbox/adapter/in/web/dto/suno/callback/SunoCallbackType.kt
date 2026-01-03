package com.jammking.loopbox.adapter.`in`.web.dto.suno.callback

enum class SunoCallbackType(val wire: String) {
    COMPLETE("complete"),
    ERROR("error"),
    FIRST("first"),
    TEXT("text");

    companion object {
        fun from(raw: String): SunoCallbackType? =
            entries.firstOrNull { it.wire.equals(raw.trim(), ignoreCase = true) }
    }
}