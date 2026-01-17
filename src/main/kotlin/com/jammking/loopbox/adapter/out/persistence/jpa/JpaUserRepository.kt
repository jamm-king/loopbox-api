package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.adapter.out.persistence.jpa.entity.UserJpaEntity
import com.jammking.loopbox.adapter.out.persistence.jpa.repository.UserJpaRepository
import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

@Repository
@Profile("postgresql")
class JpaUserRepository(
    private val repository: UserJpaRepository
) : UserRepository {
    override fun save(user: User): User {
        val saved = repository.save(UserJpaEntity.fromDomain(user))
        return saved.toDomain()
    }

    override fun findById(id: UserId): User? {
        return repository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return repository.findByEmail(email)?.toDomain()
    }
}
