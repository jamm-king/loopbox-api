package com.jammking.loopbox.domain.exception.file

import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.ErrorCategory
import com.jammking.loopbox.domain.exception.LoopboxException

class InvalidAudioFilePathException(
    val id: AudioFileId,
    val path: String
) : LoopboxException(
    errorCode = ErrorCode.INVALID_AUDIO_FILE_PATH,
    errorCategory = ErrorCategory.VALIDATION,
    message = "Audio file path cannot be blank. id=$id, path=$path"
)
