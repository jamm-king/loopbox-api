package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidMusicAiProvider(
    val provider: String
): ValidationException(
    errorCode = ErrorCode.INVALID_MUSIC_AI_PROVIDER,
    message = "Validation: Invalid music ai provider: provider=$provider"
)