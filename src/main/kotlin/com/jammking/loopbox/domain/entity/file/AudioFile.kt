package com.jammking.loopbox.domain.entity.file

import com.jammking.loopbox.domain.exception.file.InvalidAudioFilePathException
import java.util.UUID

class AudioFile(
    val id: AudioFileId = AudioFileId(UUID.randomUUID().toString()),
    val path: String
) {
    init {
        if (path.isBlank()) throw InvalidAudioFilePathException(id, path)
    }

    fun copy(
        id: AudioFileId = this.id,
        path: String = this.path
    ): AudioFile = AudioFile(
        id = id,
        path = path
    )
}