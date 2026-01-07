package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersion
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.port.out.ImageVersionRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryImageVersionRepository: ImageVersionRepository {

    private val store = ConcurrentHashMap<String, ImageVersion>()

    override fun save(version: ImageVersion): ImageVersion {
        val stored = version.copy()
        store[version.id.value] = stored
        return stored.copy()
    }

    override fun findById(versionId: ImageVersionId): ImageVersion? =
        store[versionId.value]

    override fun findByImageId(imageId: ImageId): List<ImageVersion> =
        store.values.filter { it.imageId == imageId }

    override fun deleteById(versionId: ImageVersionId) {
        store.remove(versionId.value)
    }

    override fun deleteByImageId(imageId: ImageId) {
        val targetIds = store.values.filter { it.imageId == imageId }.map { it.id }
        targetIds.forEach { store.remove(it.value) }
    }
}
