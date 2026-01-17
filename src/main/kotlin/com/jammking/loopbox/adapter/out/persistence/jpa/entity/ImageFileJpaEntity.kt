package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "image_files")
class ImageFileJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "path", nullable = false, columnDefinition = "text")
    var path: String = ""
) {
    fun toDomain(): ImageFile = ImageFile(
        id = ImageFileId(id),
        path = path
    )

    companion object {
        fun fromDomain(file: ImageFile): ImageFileJpaEntity = ImageFileJpaEntity(
            id = file.id.value,
            path = file.path
        )
    }
}
