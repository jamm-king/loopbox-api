package com.jammking.loopbox.application.exception

class ResolveImageAccessPortException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.RESOLVE_IMAGE_ACCESS_PORT,
    message = message
) {
    companion object {
        fun imageBinaryNotFound() =
            ResolveImageAccessPortException(
                code = PortErrorCode.NOT_FOUND,
                message = "Image version's local image file is not found"
            )

        fun invalidPath(path: String) =
            ResolveImageAccessPortException(
                code = PortErrorCode.INVALID_REQUEST,
                message = "Invalid image file path: $path"
            )
    }
}
