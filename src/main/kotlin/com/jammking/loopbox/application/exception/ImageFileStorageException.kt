package com.jammking.loopbox.application.exception

class ImageFileStorageException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.IMAGE_FILE_STORAGE,
    message = message
)
