package com.jammking.loopbox.application.exception

class MusicFileStorageException(
    override val code: PortErrorCode,
    override val message: String
): PortException(
    code = code,
    category = PortErrorCategory.MUSIC_FILE_STORAGE,
    message = message
)