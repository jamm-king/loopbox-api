package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.user.User
import com.jammking.loopbox.domain.entity.user.UserId

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByEmail(email: String): User?
}
