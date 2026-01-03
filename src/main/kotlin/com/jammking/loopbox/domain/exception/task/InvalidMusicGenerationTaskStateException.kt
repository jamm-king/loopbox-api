package com.jammking.loopbox.domain.exception.task

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskStatus
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.StateViolationException

class InvalidMusicGenerationTaskStateException(
    val taskId: MusicGenerationTaskId,
    val musicId: MusicId,
    val currentStatus: MusicGenerationTaskStatus,
    val attemptedAction: String
): StateViolationException(
    errorCode = ErrorCode.TASK_NOT_FOUND,
    message = "State violation: Cannot $attemptedAction when music generation task is $currentStatus: taskId=${taskId.value}, musicId=${musicId.value}"
)