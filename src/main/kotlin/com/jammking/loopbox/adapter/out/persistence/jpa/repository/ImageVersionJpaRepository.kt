package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageVersionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ImageVersionJpaRepository : JpaRepository<ImageVersionJpaEntity, String> {
    fun findByImageId(imageId: String): List<ImageVersionJpaEntity>
    fun deleteByImageId(imageId: String): Long
}
