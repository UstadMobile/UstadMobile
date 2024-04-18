package com.ustadmobile.core.domain.compress

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CompressionLevelSerializer: KSerializer<CompressionLevel> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CompressionLevel", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): CompressionLevel {
        return CompressionLevel.forValue(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: CompressionLevel) {
        encoder.encodeInt(value.value)
    }
}