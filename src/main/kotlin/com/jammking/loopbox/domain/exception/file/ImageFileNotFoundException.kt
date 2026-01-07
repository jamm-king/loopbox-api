package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class ImageFileNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.RESOURCE_NOT_FOUND,
    message = message
) {
    companion object {
        fun byImageFileId(imageFileId: ImageFileId) =
            ImageFileNotFoundException(
                message = "Not found: Image file not found: imageFileId=${imageFileId.value}"
            )
    }
}
