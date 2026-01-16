package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicVersionJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.MusicVersionJpaRepository
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersion
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.port.out.MusicVersionRepository
import org.springframework.stereotype.Repository

@Repository
class JpaMusicVersionRepository(
    private val repository: MusicVersionJpaRepository
) : MusicVersionRepository {
    override fun save(version: MusicVersion): MusicVersion {
        val saved = repository.save(MusicVersionJpaEntity.fromDomain(version))
        return saved.toDomain()
    }

    override fun findById(versionId: MusicVersionId): MusicVersion? {
        return repository.findById(versionId.value).orElse(null)?.toDomain()
    }

    override fun findByMusicId(musicId: MusicId): List<MusicVersion> {
        return repository.findByMusicId(musicId.value).map { it.toDomain() }
    }

    override fun deleteById(versionId: MusicVersionId) {
        repository.deleteById(versionId.value)
    }

    override fun deleteByMusicId(musicId: MusicId) {
        repository.deleteByMusicId(musicId.value)
    }
}
