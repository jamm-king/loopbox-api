package com.jammking.loopbox.domain.port.out

import com.jammking.loopbox.domain.entity.image.Image
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.project.ProjectId

interface ImageRepository {
    fun save(image: Image): Image
    fun findById(id: ImageId): Image?
    fun findByProjectId(projectId: ProjectId): List<Image>
    fun deleteById(id: ImageId)
}
