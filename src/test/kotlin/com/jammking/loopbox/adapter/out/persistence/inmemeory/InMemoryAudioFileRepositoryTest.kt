package com.jammking.loopbox.adapter.out.persistence.inmemeory

import com.jammking.loopbox.domain.entity.file.AudioFile
import com.jammking.loopbox.domain.entity.file.AudioFileId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class InMemoryAudioFileRepositoryTest {

    private val repository = InMemoryAudioFileRepository()

    @Test
    fun `save should store and return file`() {
        val file = AudioFile(id = AudioFileId("file-1"), path = "/tmp/audio.mp3")

        val saved = repository.save(file)
        val found = repository.findById(AudioFileId("file-1"))

        assertEquals("file-1", saved.id.value)
        assertEquals("/tmp/audio.mp3", saved.path)
        assertEquals("file-1", found?.id?.value)
        assertEquals("/tmp/audio.mp3", found?.path)
    }

    @Test
    fun `deleteById should remove file`() {
        repository.save(AudioFile(id = AudioFileId("file-1"), path = "/tmp/audio.mp3"))

        repository.deleteById(AudioFileId("file-1"))

        assertNull(repository.findById(AudioFileId("file-1")))
    }
}
