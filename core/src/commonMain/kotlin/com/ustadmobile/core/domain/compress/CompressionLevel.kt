package com.ustadmobile.core.domain.compress

import kotlinx.serialization.Serializable

@Serializable(with = CompressionLevelSerializer::class)
enum class CompressionLevel(val value: Int) {

    NONE(0), LOW(1),  MEDIUM(2), HIGH(3);

    companion object {

        fun forValue(value: Int): CompressionLevel {
            return CompressionLevel.entries.first { it.value == value }
        }

    }

}