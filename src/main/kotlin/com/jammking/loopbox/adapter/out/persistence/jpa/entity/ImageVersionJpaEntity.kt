package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.adapter.out.persistence.jpa.converter.ImageConfigConverter
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.image.ImageVersionStatus
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "image_versions")
class ImageVersionJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "image_id", nullable = false, length = 64)
    var imageId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ImageVersionStatus = ImageVersionStatus.GENERATED,
    @Convert(converter = ImageConfigConverter::class)
    @Column(name = "config", nullable = false, columnDefinition = "text")
    var config: ImageConfig? = null,
    @Column(name = "file_id", length = 64)
    var fileId: String? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): ImageVersion = ImageVersion(
        id = ImageVersionId(id),
        imageId = ImageId(imageId),
        status = status,
        config = requireNotNull(config),
        fileId = fileId?.let { ImageFileId(it) },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(version: ImageVersion): ImageVersionJpaEntity = ImageVersionJpaEntity(
            id = version.id.value,
            imageId = version.imageId.value,
            status = version.status,
            config = version.config,
            fileId = version.fileId?.value,
            createdAt = version.createdAt,
            updatedAt = version.updatedAt
        )
    }
}
