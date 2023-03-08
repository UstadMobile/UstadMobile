package com.ustadmobile.core.api.oneroster.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * ScoreStatus as per Section 4.13.6 of OneRoster spec
 *  https://www.imsglobal.org/oneroster-v11-final-specification#_Toc480452018
 */
@kotlinx.serialization.Serializable(with = ScoreStatusSerializer::class)
enum class ScoreStatus {

    EXEMPT, FULLY_GRADED, NOT_SUBMITTED, PARTIALLY_GRADED, SUBMITTED,

}

object ScoreStatusSerializer: KSerializer<ScoreStatus> {
    override fun deserialize(decoder: Decoder): ScoreStatus {
        return ScoreStatus.valueOf(decoder.decodeString().uppercase().replace(" ", "_"))
    }

    override fun serialize(encoder: Encoder, value: ScoreStatus) {
        encoder.encodeString(value.toString().lowercase().replace("_", " "))
    }

    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()
}

