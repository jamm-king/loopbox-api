package com.jammking.loopbox.application.exception

class ResolveLocalVideoPortException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.RESOLVE_LOCAL_VIDEO_PORT,
    message = message
) {
    companion object {
        fun videoBinaryNotFound() =
            ResolveLocalVideoPortException(
                code = PortErrorCode.NOT_FOUND,
                message = "Video file is not found"
            )
    }
}
