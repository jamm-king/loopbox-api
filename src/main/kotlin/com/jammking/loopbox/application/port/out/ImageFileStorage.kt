package com.jammking.loopbox.application.port.out

import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ImageFileStorage {
    fun saveFromRemoteUrl(
        remoteUrl: String,
        projectId: ProjectId,
        imageId: ImageId,
        versionId: ImageVersionId
    ): String
}
