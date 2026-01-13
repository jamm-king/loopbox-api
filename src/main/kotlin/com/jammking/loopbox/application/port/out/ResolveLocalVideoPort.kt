package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.application.port.`in`.GetVideoFileUseCase

interface ResolveLocalVideoPort {
    fun resolve(path: String): GetVideoFileUseCase.VideoStreamTarget
}
