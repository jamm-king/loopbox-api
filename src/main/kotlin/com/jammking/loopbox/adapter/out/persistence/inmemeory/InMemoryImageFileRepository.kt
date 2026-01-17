package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.port.out.ImageFileRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryImageFileRepository: ImageFileRepository {

    private val store = ConcurrentHashMap<String, ImageFile>()

    override fun save(file: ImageFile): ImageFile {
        val stored = file.copy()
        store[file.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: ImageFileId): ImageFile? =
        store[id.value]

    override fun deleteById(id: ImageFileId) {
        store.remove(id.value)
    }
}
