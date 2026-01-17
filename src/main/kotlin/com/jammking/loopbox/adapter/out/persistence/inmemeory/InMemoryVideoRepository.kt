package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("inmemory")
class InMemoryVideoRepository: VideoRepository {

    private val store = ConcurrentHashMap<String, Video>()

    override fun save(video: Video): Video {
        val stored = video.copy()
        store[video.projectId.value] = stored
        return stored.copy()
    }

    override fun findByProjectId(projectId: ProjectId): Video? =
        store[projectId.value]?.copy()

    override fun deleteByProjectId(projectId: ProjectId) {
        store.remove(projectId.value)
    }
}
