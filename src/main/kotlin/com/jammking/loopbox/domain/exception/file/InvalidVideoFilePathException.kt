package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.exception.ErrorCategory
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.LoopboxException

class InvalidVideoFilePathException(
    val id: VideoFileId,
    val path: String
): LoopboxException(
    errorCode = ErrorCode.INVALID_VIDEO_FILE_PATH,
    errorCategory = ErrorCategory.VALIDATION,
    message = "Video file path cannot be blank. id=$id, path=$path"
)
