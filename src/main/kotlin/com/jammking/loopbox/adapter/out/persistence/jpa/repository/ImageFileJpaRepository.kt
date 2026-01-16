package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ImageFileJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ImageFileJpaRepository : JpaRepository<ImageFileJpaEntity, String>
