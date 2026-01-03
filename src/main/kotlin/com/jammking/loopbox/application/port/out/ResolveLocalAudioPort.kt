package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.application.port.`in`.GetMusicVersionAudioUseCase

interface ResolveLocalAudioPort {
    fun resolve(path: String): GetMusicVersionAudioUseCase.AudioStreamTarget
}