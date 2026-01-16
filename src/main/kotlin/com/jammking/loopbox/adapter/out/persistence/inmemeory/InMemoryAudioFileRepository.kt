package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.port.out.AudioFileRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryAudioFileRepository: AudioFileRepository {

    private val store = ConcurrentHashMap<String, AudioFile>()

    override fun save(file: AudioFile): AudioFile {
        val stored = file.copy()
        store[file.id.value] = file
        return stored.copy()
    }

    override fun findById(id: AudioFileId): AudioFile? =
        store[id.value]

    override fun deleteById(id: AudioFileId) {
        store.remove(id.value)
    }
}
