package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.MusicVersionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MusicVersionJpaRepository : JpaRepository<MusicVersionJpaEntity, String> {
    fun findByMusicId(musicId: String): List<MusicVersionJpaEntity>
    fun deleteByMusicId(musicId: String): Long
}
