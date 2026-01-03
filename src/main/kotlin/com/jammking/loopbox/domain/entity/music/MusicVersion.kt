package com.jammking.loopbox.domain.entity.music

import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.exception.music.InvalidMusicVersionStateException
import java.time.Instant
import java.util.*

class MusicVersion(
    val id: MusicVersionId = MusicVersionId(UUID.randomUUID().toString()),
    val musicId: MusicId,
    status: MusicVersionStatus = MusicVersionStatus.GENERATED,
    val config: MusicConfig,
    fileId: AudioFileId? = null,
    val durationSeconds: Int = 0,
    val createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now()
) {

    var status: MusicVersionStatus = status
        private set

    var fileId: AudioFileId? = fileId
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun startFileDownload() {
        if(!isGenerated()) throw InvalidMusicVersionStateException(id, status, "start file download")

        this.status = MusicVersionStatus.FILE_DOWNLOADING
        this.updatedAt = Instant.now()
    }

    fun completeFileDownload(fileId: AudioFileId) {
        if(!isFileDownloading()) throw InvalidMusicVersionStateException(id, status, "complete file download")

        this.fileId = fileId
        this.status = MusicVersionStatus.READY
        this.updatedAt = Instant.now()
    }

    fun failFileDownload() {
        if(!isFileDownloading()) throw InvalidMusicVersionStateException(id, status, "fail file download")

        this.status = MusicVersionStatus.FILE_DOWNLOAD_FAILED
        this.updatedAt = Instant.now()
    }

    fun copy(
        id: MusicVersionId = this.id,
        musicId: MusicId = this.musicId,
        status: MusicVersionStatus = this.status,
        config: MusicConfig = this.config,
        fileId: AudioFileId? = this.fileId,
        durationSeconds: Int = this.durationSeconds,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt
    ): MusicVersion = MusicVersion(
        id = id,
        musicId = musicId,
        status = status,
        config = config,
        fileId = fileId,
        durationSeconds = durationSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun isGenerated() = status == MusicVersionStatus.GENERATED
    fun isGenerationFailed() = status == MusicVersionStatus.GENERATION_FAILED
    fun isFileDownloading() = status == MusicVersionStatus.FILE_DOWNLOADING
    fun isFileDownloadFailed() = status == MusicVersionStatus.FILE_DOWNLOAD_FAILED
    fun isReady() = status == MusicVersionStatus.READY
}