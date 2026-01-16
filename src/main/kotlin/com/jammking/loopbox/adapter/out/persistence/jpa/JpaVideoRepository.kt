package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.VideoJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.VideoJpaRepository
import com.jammking.loopbox.domain.entity.project.ProjectId
import com.jammking.loopbox.domain.entity.video.Video
import com.jammking.loopbox.domain.port.out.VideoRepository
import org.springframework.stereotype.Repository

@Repository
class JpaVideoRepository(
    private val repository: VideoJpaRepository
) : VideoRepository {
    override fun save(video: Video): Video {
        val saved = repository.save(VideoJpaEntity.fromDomain(video))
        return saved.toDomain()
    }

    override fun findByProjectId(projectId: ProjectId): Video? {
        return repository.findByProjectId(projectId.value)?.toDomain()
    }

    override fun deleteByProjectId(projectId: ProjectId) {
        repository.deleteByProjectId(projectId.value)
    }
}
