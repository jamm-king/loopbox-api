package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageVersionJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.ImageVersionJpaRepository
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import org.springframework.stereotype.Repository

@Repository
class JpaImageVersionRepository(
    private val repository: ImageVersionJpaRepository
) : ImageVersionRepository {
    override fun save(version: ImageVersion): ImageVersion {
        val saved = repository.save(ImageVersionJpaEntity.fromDomain(version))
        return saved.toDomain()
    }

    override fun findById(versionId: ImageVersionId): ImageVersion? {
        return repository.findById(versionId.value).orElse(null)?.toDomain()
    }

    override fun findByImageId(imageId: ImageId): List<ImageVersion> {
        return repository.findByImageId(imageId.value).map { it.toDomain() }
    }

    override fun deleteById(versionId: ImageVersionId) {
        repository.deleteById(versionId.value)
    }

    override fun deleteByImageId(imageId: ImageId) {
        repository.deleteByImageId(imageId.value)
    }
}
