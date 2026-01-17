package com.jammking.loopbox.adapter.out.persistence.jpa

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import com.jammking.loopbox.domain.entity.file.ImageFile
import com.jammking.loopbox.domain.entity.file.ImageFileId
import com.jammking.loopbox.domain.entity.file.VideoFile
import com.jammking.loopbox.domain.entity.file.VideoFileId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("postgresql")
@Import(
    JpaAudioFileRepository::class,
    JpaImageFileRepository::class,
    JpaVideoFileRepository::class
)
class JpaFileRepositoriesTest {

    @Autowired
    private lateinit var audioFileRepository: JpaAudioFileRepository

    @Autowired
    private lateinit var imageFileRepository: JpaImageFileRepository

    @Autowired
    private lateinit var videoFileRepository: JpaVideoFileRepository

    @Test
    fun `audio file repository should save and delete`() {
        val file = AudioFile(id = AudioFileId("audio-1"), path = "/tmp/audio.mp3")

        val saved = audioFileRepository.save(file)
        val found = audioFileRepository.findById(AudioFileId("audio-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("/tmp/audio.mp3", found?.path)

        audioFileRepository.deleteById(AudioFileId("audio-1"))
        assertNull(audioFileRepository.findById(AudioFileId("audio-1")))
    }

    @Test
    fun `image file repository should save and delete`() {
        val file = ImageFile(id = ImageFileId("image-file-1"), path = "/tmp/image.png")

        val saved = imageFileRepository.save(file)
        val found = imageFileRepository.findById(ImageFileId("image-file-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("/tmp/image.png", found?.path)

        imageFileRepository.deleteById(ImageFileId("image-file-1"))
        assertNull(imageFileRepository.findById(ImageFileId("image-file-1")))
    }

    @Test
    fun `video file repository should save and delete`() {
        val file = VideoFile(id = VideoFileId("video-file-1"), path = "/tmp/video.mp4")

        val saved = videoFileRepository.save(file)
        val found = videoFileRepository.findById(VideoFileId("video-file-1"))

        assertEquals(saved.id.value, found?.id?.value)
        assertEquals("/tmp/video.mp4", found?.path)

        videoFileRepository.deleteById(VideoFileId("video-file-1"))
        assertNull(videoFileRepository.findById(VideoFileId("video-file-1")))
    }
}
