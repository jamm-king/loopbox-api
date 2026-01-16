package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.RefreshTokenJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, String> {
    fun findByToken(token: String): RefreshTokenJpaEntity?
    fun deleteByToken(token: String): Long
    fun deleteByUserId(userId: String): Long
}
