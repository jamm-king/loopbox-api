package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.VideoFileJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.VideoFileJpaRepository
import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("postgresql")
class JpaVideoFileRepository(
    private val repository: VideoFileJpaRepository
) : VideoFileRepository {
    override fun save(file: VideoFile): VideoFile {
        val saved = repository.save(VideoFileJpaEntity.fromDomain(file))
        return saved.toDomain()
    }

    override fun findById(id: VideoFileId): VideoFile? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun deleteById(id: VideoFileId) {
        repository.deleteById(id.value)
    }
}
