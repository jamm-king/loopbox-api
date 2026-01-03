package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.music.WebMusic
import com.jammking.loopbox.adapter.`in`.web.dto.music.WebMusicConfig
import com.jammking.loopbox.adapter.`in`.web.dto.music.WebMusicVersion
import com.jammking.loopbox.application.port.`in`.MusicQueryUseCase
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicConfig
import com.jammking.loopbox.domain.entity.music.MusicVersion

object WebMusicMapper {

    fun Music.toWeb() =
        WebMusic(
            id = id.value,
            status = status.name
        )

    fun MusicVersion.toWeb() =
        WebMusicVersion(
            id = id.value,
            fileId = fileId?.value,
            config = config.toWeb(),
            durationSeconds = durationSeconds
        )

    fun MusicConfig.toWeb() =
        WebMusicConfig(
            mood = mood,
            bpm = bpm,
            melody = melody,
            harmony = harmony,
            bass = bass,
            beat = beat
        )
}