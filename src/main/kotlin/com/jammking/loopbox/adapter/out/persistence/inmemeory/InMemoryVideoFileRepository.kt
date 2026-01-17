package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import com.jammking.loopbox.domain.port.out.VideoFileRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryVideoFileRepository: VideoFileRepository {

    private val store = ConcurrentHashMap<String, VideoFile>()

    override fun save(file: VideoFile): VideoFile {
        val stored = file.copy()
        store[file.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: VideoFileId): VideoFile? =
        store[id.value]?.copy()

    override fun deleteById(id: VideoFileId) {
        store.remove(id.value)
    }
}
