package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.auth.RefreshToken
import com.jammking.loopbox.domain.entity.user.UserId

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findByToken(token: String): RefreshToken?
    fun deleteByToken(token: String)
    fun deleteByUserId(userId: UserId)
}
