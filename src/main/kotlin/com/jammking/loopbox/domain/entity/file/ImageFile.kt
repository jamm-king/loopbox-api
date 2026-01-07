package com.jammking.loopbox.domain.entity.file

import com.jammking.loopbox.domain.exception.file.InvalidImageFilePathException
import java.util.UUID

class ImageFile(
    val id: ImageFileId = ImageFileId(UUID.randomUUID().toString()),
    val path: String
) {
    init {
        if (path.isBlank()) throw InvalidImageFilePathException(id, path)
    }

    fun copy(
        id: ImageFileId = this.id,
        path: String = this.path
    ): ImageFile = ImageFile(
        id = id,
        path = path
    )
}
