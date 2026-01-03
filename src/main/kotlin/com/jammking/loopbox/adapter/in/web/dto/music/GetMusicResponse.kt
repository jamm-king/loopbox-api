package com.jammking.loopbox.adapter.`in`.web.dto.music

data class GetMusicResponse(
    val music: WebMusic,
    val versions: List<WebMusicVersion>
) { companion object }
