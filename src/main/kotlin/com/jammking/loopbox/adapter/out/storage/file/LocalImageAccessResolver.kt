package com.jammking.loopbox.adapter.out.storage.file

import com.jammking.loopbox.application.exception.ResolveImageAccessPortException
import com.jammking.loopbox.application.port.out.ResolveImageAccessPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class LocalImageAccessResolver(
    @Value("\${loopbox.storage.image-dir}")
    private val imageBaseDir: String,
    @Value("\${loopbox.storage.image-public-base-url}")
    private val imagePublicBaseUrl: String
): ResolveImageAccessPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun resolve(path: String): ResolveImageAccessPort.ImageAccessTarget {
        val basePath = Paths.get(imageBaseDir).toAbsolutePath().normalize()
        val filePath = safePath(path)

        if (!filePath.startsWith(basePath)) {
            log.warn("Image path is outside base dir: base={}, path={}", basePath, filePath)
            throw ResolveImageAccessPortException.invalidPath(path)
        }
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
            log.error("Image binary missing or unreadable: path={}", filePath)
            throw ResolveImageAccessPortException.imageBinaryNotFound()
        }

        val relative = basePath.relativize(filePath).toString().replace('\\', '/')
        val baseUrl = imagePublicBaseUrl.trimEnd('/')

        return ResolveImageAccessPort.ImageAccessTarget(
            url = "$baseUrl/$relative"
        )
    }

    private fun safePath(raw: String): Path =
        Paths.get(raw).toAbsolutePath().normalize()
}
