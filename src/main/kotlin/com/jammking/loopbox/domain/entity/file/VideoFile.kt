package com.jammking.loopbox.domain.entity.file

import com.jammking.loopbox.domain.exception.file.InvalidVideoFilePathException
import java.util.UUID

class VideoFile(
    val id: VideoFileId = VideoFileId(UUID.randomUUID().toString()),
    val path: String
) {
    init {
        if (path.isBlank()) throw InvalidVideoFilePathException(id, path)
    }

    fun copy(
        id: VideoFileId = this.id,
        path: String = this.path
    ): VideoFile = VideoFile(
        id = id,
        path = path
    )
}
