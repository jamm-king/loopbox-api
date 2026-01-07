package com.jammking.loopbox.application.exception

import com.jammking.loopbox.domain.entity.task.ImageAiProvider

class ImageAiClientException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.IMAGE_AI_CLIENT,
    message = message
) {
    companion object {

        // --- TEMPORARY_UNAVAILABLE ---

        fun failConnection(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.TEMPORARY_UNAVAILABLE,
                message = "Connection failed to ${provider.name}"
            )

        fun invalidHttpCode(provider: ImageAiProvider, code: Int) =
            ImageAiClientException(
                code = PortErrorCode.TEMPORARY_UNAVAILABLE,
                message = "${provider.name} returned invalid http code: $code"
            )

        // --- PROTOCOL_VIOLATION ---

        fun invalidSystemCode(provider: ImageAiProvider, code: Int) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} returned invalid system code: $code"
            )

        fun emptyResponseBody(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} returned empty response body"
            )

        fun invalidJson(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid JSON returned by ${provider.name}"
            )

        fun invalidSchema(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid schema from ${provider.name}"
            )

        fun invalidPayloadState(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid payload state from ${provider.name}"
            )

        fun missingTaskId(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} response has no taskId"
            )

        // --- QUOTA_EXCEEDED ---

        fun outOfCredit(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.QUOTA_EXCEEDED,
                message = "Out of credit for ${provider.name}"
            )

        // --- UNKNOWN ---

        fun unknown(provider: ImageAiProvider) =
            ImageAiClientException(
                code = PortErrorCode.UNKNOWN,
                message = "Unknown error with ${provider.name}"
            )
    }
}
