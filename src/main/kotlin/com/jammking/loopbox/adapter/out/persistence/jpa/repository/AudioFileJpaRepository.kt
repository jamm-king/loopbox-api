package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.AudioFileJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AudioFileJpaRepository : JpaRepository<AudioFileJpaEntity, String>
