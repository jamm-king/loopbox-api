package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class DuplicateVersionIdException(
    val versionId: MusicVersionId
): StateViolationException(
    errorCode = ErrorCode.DUPLICATE_VERSION_ID,
    message = "State violation: Duplicate music version id: musicVersionId=${versionId.value}"
)