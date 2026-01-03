package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.music.MusicVersionStatus
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidMusicVersionStateException(
    val versionId: MusicVersionId,
    val currentStatus: MusicVersionStatus,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.INVALID_MUSIC_VERSION_STATE,
    message = "State Violation: Cannot $attemptedAction when music version is $currentStatus"
)