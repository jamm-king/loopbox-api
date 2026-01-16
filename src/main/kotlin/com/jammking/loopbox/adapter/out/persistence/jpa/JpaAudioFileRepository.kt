package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.AudioFileJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.AudioFileJpaRepository
import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import org.springframework.stereotype.Repository

@Repository
class JpaAudioFileRepository(
    private val repository: AudioFileJpaRepository
) : AudioFileRepository {
    override fun save(file: AudioFile): AudioFile {
        val saved = repository.save(AudioFileJpaEntity.fromDomain(file))
        return saved.toDomain()
    }

    override fun findById(id: AudioFileId): AudioFile? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun deleteById(id: AudioFileId) {
        repository.deleteById(id.value)
    }
}
