package com.jammking.loopbox.adapter.out.persistence.jpa.repository

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.ProjectJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectJpaRepository : JpaRepository<ProjectJpaEntity, String>
