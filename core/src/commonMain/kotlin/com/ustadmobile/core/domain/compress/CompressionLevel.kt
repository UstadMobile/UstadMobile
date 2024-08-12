package com.ustadmobile.core.domain.compress

import kotlinx.serialization.Serializable

@Serializable(with = CompressionLevelSerializer::class)
enum class CompressionLevel(val value: Int) {

    NONE(0), LOWEST(1), LOW(2),  MEDIUM(3), HIGH(4), HIGHEST(5);

    companion object {

        fun forValue(value: Int): CompressionLevel {
            return CompressionLevel.entries.first { it.value == value }
        }

    }

}