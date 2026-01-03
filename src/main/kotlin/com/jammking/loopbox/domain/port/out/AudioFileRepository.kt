package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId

interface AudioFileRepository {
    fun save(file: AudioFile): AudioFile
    fun findById(id: AudioFileId): AudioFile?
    fun deleteById(id: AudioFileId)
}