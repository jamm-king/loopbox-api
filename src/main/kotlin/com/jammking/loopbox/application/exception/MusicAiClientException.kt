package com.jammking.loopbox.application.exception

import com.jammking.loopbox.domain.entity.task.MusicAiProvider

class MusicAiClientException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.MUSIC_AI_CLIENT,
    message = message
) {
    companion object {

        // --- TEMPORARY_UNAVAILABLE ---

        fun failConnection(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.TEMPORARY_UNAVAILABLE,
                message = "Connection failed to ${provider.name}"
            )

        fun invalidHttpCode(provider: MusicAiProvider, code: Int) =
            MusicAiClientException(
                code = PortErrorCode.TEMPORARY_UNAVAILABLE,
                message = "${provider.name} returned invalid http code: $code"
            )

        // --- PROTOCOL_VIOLATION ---

        fun invalidSystemCode(provider: MusicAiProvider, code: Int) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} returned invalid system code: $code"
            )

        fun emptyResponseBody(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} returned empty response body"
            )

        fun invalidJson(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid JSON returned by ${provider.name}"
            )

        fun invalidSchema(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid schema from ${provider.name}"
            )

        fun invalidPayloadState(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "Invalid payload state from ${provider.name}"
            )

        fun missingTaskId(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.PROTOCOL_VIOLATION,
                message = "${provider.name} response has no taskId"
            )

        // --- QUOTA_EXCEEDED ---

        fun outOfCredit(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.QUOTA_EXCEEDED,
                message = "Out of credit for ${provider.name}"
            )

        // --- UNKNOWN ---

        fun unknown(provider: MusicAiProvider) =
            MusicAiClientException(
                code = PortErrorCode.UNKNOWN,
                message = "Unknown error with ${provider.name}"
            )
    }
}