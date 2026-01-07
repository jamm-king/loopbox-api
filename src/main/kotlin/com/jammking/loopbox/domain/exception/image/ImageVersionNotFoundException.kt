package com.jammking.loopbox.domain.exception.image

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.NotFoundException

class ImageVersionNotFoundException(
    override val message: String
): NotFoundException(
    errorCode = ErrorCode.VERSION_NOT_FOUND,
    message = message
) {
    companion object {

        fun byImageId(imageId: ImageId) =
            ImageVersionNotFoundException(
                "Not found: ImageVersion not found for image: imageId=${imageId.value}"
            )

        fun byVersionId(versionId: ImageVersionId) =
            ImageVersionNotFoundException(
                "Not found: ImageVersion not found: versionId=${versionId.value}"
            )
    }
}
