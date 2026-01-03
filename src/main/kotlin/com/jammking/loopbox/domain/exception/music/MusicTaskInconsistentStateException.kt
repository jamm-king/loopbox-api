package com.jammking.loopbox.domain.exception.music

import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.task.MusicGenerationTaskId
import com.jammking.loopbox.domain.exception.ErrorCode
import com.jammking.loopbox.domain.exception.InconsistentStateException

class MusicTaskInconsistentStateException(
    override val message: String
): InconsistentStateException(
    errorCode = ErrorCode.INCONSISTENT_MUSIC_TASK_RELATION,
    message = message
) {
    companion object {
        fun musicMissingForTask(taskId: MusicGenerationTaskId, musicId: MusicId) =
            MusicTaskInconsistentStateException(
                "Inconsistent state: Music not found for existing task: taskId=${taskId.value}, musicId=${musicId.value}"
            )
    }
}