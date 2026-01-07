package com.jammking.loopbox.domain.entity.image

import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.exception.image.InvalidImageVersionStateException
import java.time.Instant
import java.util.UUID

class ImageVersion(
    val id: ImageVersionId = ImageVersionId(UUID.randomUUID().toString()),
    val imageId: ImageId,
    status: ImageVersionStatus = ImageVersionStatus.GENERATED,
    val config: ImageConfig,
    fileId: ImageFileId? = null,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    var status: ImageVersionStatus = status
        private set

    var fileId: ImageFileId? = fileId
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun startFileDownload() {
        if(!isGenerated()) throw InvalidImageVersionStateException(id, status, "start file download")

        this.status = ImageVersionStatus.FILE_DOWNLOADING
        this.updatedAt = Instant.now()
    }

    fun completeFileDownload(fileId: ImageFileId) {
        if(!isFileDownloading()) throw InvalidImageVersionStateException(id, status, "complete file download")

        this.fileId = fileId
        this.status = ImageVersionStatus.READY
        this.updatedAt = Instant.now()
    }

    fun failFileDownload() {
        if(!isFileDownloading()) throw InvalidImageVersionStateException(id, status, "fail file download")

        this.status = ImageVersionStatus.FILE_DOWNLOAD_FAILED
        this.updatedAt = Instant.now()
    }

    fun copy(
        id: ImageVersionId = this.id,
        imageId: ImageId = this.imageId,
        status: ImageVersionStatus = this.status,
        config: ImageConfig = this.config,
        fileId: ImageFileId? = this.fileId,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): ImageVersion = ImageVersion(
        id = id,
        imageId = imageId,
        status = status,
        config = config,
        fileId = fileId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun isGenerated() = status == ImageVersionStatus.GENERATED
    fun isGenerationFailed() = status == ImageVersionStatus.GENERATION_FAILED
    fun isFileDownloading() = status == ImageVersionStatus.FILE_DOWNLOADING
    fun isFileDownloadFailed() = status == ImageVersionStatus.FILE_DOWNLOAD_FAILED
    fun isReady() = status == ImageVersionStatus.READY
}
