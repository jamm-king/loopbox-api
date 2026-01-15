package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId
import com.jammking.loopbox.domain.port.out.UserRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryUserRepository: UserRepository {

    private val store = ConcurrentHashMap<String, User>()
    private val emailIndex = ConcurrentHashMap<String, String>()

    override fun save(user: User): User {
        val stored = user.copy()
        store[user.id.value] = stored
        emailIndex[user.email] = user.id.value
        return stored.copy()
    }

    override fun findById(id: UserId): User? =
        store[id.value]?.copy()

    override fun findByEmail(email: String): User? =
        emailIndex[email]?.let { id -> store[id]?.copy() }
}
