package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.RefreshTokenJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.RefreshTokenJpaRepository
import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import org.springframework.stereotype.Repository

@Repository
class JpaRefreshTokenRepository(
    private val repository: RefreshTokenJpaRepository
) : RefreshTokenRepository {
    override fun save(token: RefreshToken): RefreshToken {
        val saved = repository.save(RefreshTokenJpaEntity.fromDomain(token))
        return saved.toDomain()
    }

    override fun findByToken(token: String): RefreshToken? {
        return repository.findByToken(token)?.toDomain()
    }

    override fun deleteByToken(token: String) {
        repository.deleteByToken(token)
    }

    override fun deleteByUserId(userId: UserId) {
        repository.deleteByUserId(userId.value)
    }
}
