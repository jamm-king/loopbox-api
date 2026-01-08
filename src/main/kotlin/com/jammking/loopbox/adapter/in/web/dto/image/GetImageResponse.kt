package com.jammking.loopbox.adapter.`in`.web.dto.image

import com.jammking.loopbox.application.port.`in`.ImageQueryUseCase
import com.jammking.loopbox.adapter.`in`.web.mapper.WebImageMapper.toWeb

data class GetImageResponse(
    val image: WebImage,
    val versions: List<WebImageVersion>
) {
    companion object {
        fun from(result: ImageQueryUseCase.GetImageDetailResult) =
            GetImageResponse(
                image = result.image.toWeb(),
                versions = result.versions.map { version ->
                    version.toWeb(result.versionUrls[version.id])
                }
            )
    }
}
