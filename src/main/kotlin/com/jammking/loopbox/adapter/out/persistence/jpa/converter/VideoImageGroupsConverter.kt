package com.jammking.loopbox.adapter.out.persistence.jpa.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.jammking.loopbox.domain.entity.video.VideoImageGroup
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class VideoImageGroupsConverter : AttributeConverter<List<VideoImageGroup>, String> {
    override fun convertToDatabaseColumn(attribute: List<VideoImageGroup>): String {
        return JpaJsonMapper.mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): List<VideoImageGroup> {
        return JpaJsonMapper.mapper.readValue(dbData, object : TypeReference<List<VideoImageGroup>>() {})
    }
}
