package com.jammking.loopbox.adapter.out.notification

import com.jammking.loopbox.application.port.out.NotificationPort
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.music.MusicId
import com.jammking.loopbox.domain.entity.music.MusicVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingNotificationAdapter: NotificationPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun notifyVersionGenerationCompleted(
        projectId: ProjectId,
        musicId: MusicId,
        versionIds: List<MusicVersionId>
    ) {
        log.info(
            "[Notification] Music version generation completed: projectId={}, musicId={}, versionIds={}",
            projectId.value, musicId.value, versionIds.map { it.value }
        )
    }

    override fun notifyVersionGenerationFailed(
        projectId: ProjectId,
        musicId: MusicId
    ) {
        log.info(
            "[Notification] Music version generation failed: projectId={}, musicId={}",
            projectId.value, musicId.value
        )
    }

    override fun notifyImageVersionGenerationCompleted(
        projectId: ProjectId,
        imageId: ImageId,
        versionIds: List<ImageVersionId>
    ) {
        log.info(
            "[Notification] Image version generation completed: projectId={}, imageId={}, versionIds={}",
            projectId.value, imageId.value, versionIds.map { it.value }
        )
    }

    override fun notifyImageVersionGenerationFailed(
        projectId: ProjectId,
        imageId: ImageId
    ) {
        log.info(
            "[Notification] Image version generation failed: projectId={}, imageId={}",
            projectId.value, imageId.value
        )
    }


}
