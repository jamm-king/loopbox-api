package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.RefreshTokenRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryRefreshTokenRepository: RefreshTokenRepository {

    private val store = ConcurrentHashMap<String, RefreshToken>()

    override fun save(token: RefreshToken): RefreshToken {
        store[token.token] = token
        return token
    }

    override fun findByToken(token: String): RefreshToken? =
        store[token]

    override fun deleteByToken(token: String) {
        store.remove(token)
    }

    override fun deleteByUserId(userId: UserId) {
        store.entries.removeIf { it.value.userId == userId }
    }
}
