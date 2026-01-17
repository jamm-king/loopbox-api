package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.ImageJpaRepository
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.port.out.ImageRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("postgresql")
class JpaImageRepository(
    private val repository: ImageJpaRepository
) : ImageRepository {
    override fun save(image: Image): Image {
        val saved = repository.save(ImageJpaEntity.fromDomain(image))
        return saved.toDomain()
    }

    override fun findById(id: ImageId): Image? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByProjectId(projectId: ProjectId): List<Image> {
        return repository.findByProjectId(projectId.value).map { it.toDomain() }
    }

    override fun deleteById(id: ImageId) {
        repository.deleteById(id.value)
    }
}
