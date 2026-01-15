package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId

interface VideoFileRepository {
    fun save(file: VideoFile): VideoFile
    fun findById(id: VideoFileId): VideoFile?
    fun deleteById(id: VideoFileId)
}
