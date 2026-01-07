package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId

interface ImageFileRepository {
    fun save(file: ImageFile): ImageFile
    fun findById(id: ImageFileId): ImageFile?
    fun deleteById(id: ImageFileId)
}
