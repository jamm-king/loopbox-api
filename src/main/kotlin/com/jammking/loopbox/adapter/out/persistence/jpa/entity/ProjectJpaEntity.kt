package com.jammking.loopbox.adapter.out.persistence.jpa.entity

import com.jammking.loopbox.domain.entity.project.Project
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.project.ProjectStatus
import com.jammking.loopbox.domain.entity.user.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "projects")
class ProjectJpaEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    var id: String = "",
    @Column(name = "owner_user_id", nullable = false, length = 64)
    var ownerUserId: String = "",
    @Column(name = "title", nullable = false, length = 255)
    var title: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: ProjectStatus = ProjectStatus.DRAFT,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): Project = Project(
        id = ProjectId(id),
        ownerUserId = UserId(ownerUserId),
        title = title,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(project: Project): ProjectJpaEntity = ProjectJpaEntity(
            id = project.id.value,
            ownerUserId = project.ownerUserId.value,
            title = project.title,
            status = project.status,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}
