package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.entity.task.ImageAiProvider
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ValidationException

class InvalidImageAiProvider(
    val provider: String
): ValidationException(
    errorCode = ErrorCode.INVALID_IMAGE_AI_PROVIDER,
    message = "Invalid Image AI Provider: $provider. Supported providers: ${ImageAiProvider.entries.joinToString()}"
)
