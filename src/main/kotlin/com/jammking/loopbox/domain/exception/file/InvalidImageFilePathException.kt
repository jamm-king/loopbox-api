package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.exception.ErrorCategory
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.LoopboxException

class InvalidImageFilePathException(
    val id: ImageFileId,
    val path: String
): LoopboxException(
    errorCode = ErrorCode.INVALID_IMAGE_FILE_PATH,
    errorCategory = ErrorCategory.VALIDATION,
    message = "Image file path cannot be blank. id=$id, path=$path"
)
