package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ImageJpaRepository : JpaRepository<ImageJpaEntity, String> {
    fun findByProjectId(projectId: String): List<ImageJpaEntity>
}
