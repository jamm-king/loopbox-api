package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.VideoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface VideoJpaRepository : JpaRepository<VideoJpaEntity, String> {
    fun findByProjectId(projectId: String): VideoJpaEntity?
    fun deleteByProjectId(projectId: String): Long
}
