package com.jammking.loopbox.adapter.`in`.web.mapper

import com.jammking.loopbox.adapter.`in`.web.dto.image.WebImage
import com.jammking.loopbox.adapter.`in`.web.dto.image.WebImageConfig
import com.jammking.loopbox.adapter.`in`.web.dto.image.WebImageVersion
import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageConfig
import com.jammking.loopbox.domain.entity.image.ImageVersion

object WebImageMapper {

    fun Image.toWeb() =
        WebImage(
            id = id.value,
            status = status.name
        )

    fun ImageVersion.toWeb(url: String? = null) =
        WebImageVersion(
            id = id.value,
            fileId = fileId?.value,
            url = url,
            config = config.toWeb()
        )

    fun ImageConfig.toWeb() =
        WebImageConfig(
            description = description,
            width = width,
            height = height
        )
}
