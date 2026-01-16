package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.adapter.out.persistence.jpa.converter.ImageConfigConverter
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageOperation
import com.jammking.loopbox.domain.entity.image.ImageStatus
import com.jammking.loopbox.domain.entity.project.ProjectId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "images")
class ImageJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "project_id", nullable = false, length = 64)
    var projectId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ImageStatus = ImageStatus.IDLE,
    @Convert(converter = ImageConfigConverter::class)
    @Column(name = "requested_config", columnDefinition = "text")
    var requestedConfig: ImageConfig? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "last_operation", length = 32)
    var lastOperation: ImageOperation? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): Image = Image(
        id = ImageId(id),
        projectId = ProjectId(projectId),
        status = status,
        requestedConfig = requestedConfig,
        lastOperation = lastOperation,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(image: Image): ImageJpaEntity = ImageJpaEntity(
            id = image.id.value,
            projectId = image.projectId.value,
            status = image.status,
            requestedConfig = image.requestedConfig,
            lastOperation = image.lastOperation,
            createdAt = image.createdAt,
            updatedAt = image.updatedAt
        )
    }
}
