package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidMusicStateException(
    val music: Music,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_MUSIC_STATE,
    message = "State violation: Cannot $attemptedAction: musicId=${music.id.value}, status=${music.status.name}, lastOperation=${music.lastOperation?.name}, requestedConfig=${music.requestedConfig}"
)