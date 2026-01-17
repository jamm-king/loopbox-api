package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MusicJpaRepository : JpaRepository<MusicJpaEntity, String> {
    fun findByProjectId(projectId: String): List<MusicJpaEntity>
}
