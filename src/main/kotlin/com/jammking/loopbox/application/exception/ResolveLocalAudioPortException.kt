package com.jammking.loopbox.application.exception

class ResolveLocalAudioPortException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.RESOLVE_LOCAL_AUDIO_PORT,
    message = message
){
    companion object {
        fun audioBinaryNotFound() =
            ResolveLocalAudioPortException(
                code = PortErrorCode.NOT_FOUND,
                message = "Music version's local audio file is not found"
            )
    }
}