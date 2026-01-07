package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.port.out.ImageFileStorage
import com.jammking.loopbox.domain.entity.image.ImageId
import com.jammking.loopbox.domain.entity.image.ImageVersionId
import com.jammking.loopbox.domain.entity.project.ProjectId
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@Component
class LocalImageFileStorage(
    @Value("\${loopbox.storage.image-dir}")
    private val imageBaseDir: String,
    private val okHttpClient: OkHttpClient = OkHttpClient()
): ImageFileStorage {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveFromRemoteUrl(
        remoteUrl: String,
        projectId: ProjectId,
        imageId: ImageId,
        versionId: ImageVersionId
    ): String {

        val dir = Paths.get(imageBaseDir, projectId.value, imageId.value)
        try {
            Files.createDirectories(dir)
        } catch(e: Exception) {
            log.error("Failed To create directories for path={}", dir.toAbsolutePath(), e)
            return ""
        }

        val fileName = "${versionId.value}.png"
        val target = dir.resolve(fileName)

        return try {
            val request = Request.Builder()
                .url(URI(remoteUrl).toURL())
                .build()

            log.info("Downloading image: url={}, target={}", remoteUrl, target.toAbsolutePath())

            okHttpClient.newCall(request).execute().use { response ->
                if(!response.isSuccessful) {
                    log.warn(
                        "Failed to download image. url={}, code={}, message={}",
                        remoteUrl, response.code, response.message
                    )
                    return ""
                }

                val body = response.body
                if(body == null) {
                    log.warn("Empty body when downloading image. url={}", remoteUrl)
                    return ""
                }

                body.byteStream().use { input ->
                    Files.newOutputStream(
                        target,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    ).use { output ->
                        input.copyTo(output)
                    }
                }

                target.toString()
            }
        } catch(e: Exception) {
            log.error("Failed to download image from url={}", remoteUrl, e)
            ""
        }
    }
}
