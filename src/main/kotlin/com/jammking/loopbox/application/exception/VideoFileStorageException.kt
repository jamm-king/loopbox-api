package com.jammking.loopbox.application.exception

class VideoFileStorageException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.VIDEO_FILE_STORAGE,
    message = message
)
