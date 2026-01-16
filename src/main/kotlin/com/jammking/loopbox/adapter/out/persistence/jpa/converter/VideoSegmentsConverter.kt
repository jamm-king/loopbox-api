package com.jammking.loopbox.adapter.out.persistence.jpa.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.jammking.loopbox.domain.entity.video.VideoSegment
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class VideoSegmentsConverter : AttributeConverter<List<VideoSegment>, String> {
    override fun convertToDatabaseColumn(attribute: List<VideoSegment>): String {
        return JpaJsonMapper.mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): List<VideoSegment> {
        return JpaJsonMapper.mapper.readValue(dbData, object : TypeReference<List<VideoSegment>>() {})
    }
}
