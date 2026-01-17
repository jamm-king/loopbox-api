package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.port.out.ImageRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryImageRepository: ImageRepository {

    private val store = ConcurrentHashMap<String, Image>()

    override fun save(image: Image): Image {
        val stored = image.copy()
        store[image.id.value] = stored
        return stored.copy()
    }

    override fun findById(id: ImageId): Image? =
        store[id.value]

    override fun findByProjectId(projectId: ProjectId): List<Image> =
        store.values.filter { it.projectId == projectId }

    override fun deleteById(id: ImageId) {
        store.remove(id.value)
    }
}
