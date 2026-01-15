package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.user.WebUser
import com.jammking.loopbox.domain.entity.user.User

object WebUserMapper {
    fun User.toWeb() =
        WebUser(
            id = id.value,
            email = email
        )
}
