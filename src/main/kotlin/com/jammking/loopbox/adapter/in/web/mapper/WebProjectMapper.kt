package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.project.WebProject
import com.jammking.loopbox.domain.entity.project.Project

object WebProjectMapper {
    fun Project.toWeb() =
        WebProject(
            id = id.value,
            ownerUserId = ownerUserId.value,
            title = title,
            status = status.name
        )
}
