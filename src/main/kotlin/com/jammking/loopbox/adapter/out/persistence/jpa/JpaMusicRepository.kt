package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.MusicJpaRepository
import com.jammking.loopbox.domain.entity.music.Music
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.port.out.MusicRepository
import org.springframework.stereotype.Repository

@Repository
class JpaMusicRepository(
    private val repository: MusicJpaRepository
) : MusicRepository {
    override fun save(music: Music): Music {
        val saved = repository.save(MusicJpaEntity.fromDomain(music))
        return saved.toDomain()
    }

    override fun findById(id: MusicId): Music? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByProjectId(projectId: ProjectId): List<Music> {
        return repository.findByProjectId(projectId.value).map { it.toDomain() }
    }

    override fun deleteById(id: MusicId) {
        repository.deleteById(id.value)
    }
}
