package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageFileJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.ImageFileJpaRepository
import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("postgresql")
class JpaImageFileRepository(
    private val repository: ImageFileJpaRepository
) : ImageFileRepository {
    override fun save(file: ImageFile): ImageFile {
        val saved = repository.save(ImageFileJpaEntity.fromDomain(file))
        return saved.toDomain()
    }

    override fun findById(id: ImageFileId): ImageFile? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun deleteById(id: ImageFileId) {
        repository.deleteById(id.value)
    }
}
