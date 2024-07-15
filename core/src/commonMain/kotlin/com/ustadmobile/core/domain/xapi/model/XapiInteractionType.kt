package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Valid InteractionType properties as per:
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 */
@Serializable(with = XapiInteractionTypeSerializer::class)
enum class XapiInteractionType(
    val jsonFieldValue: String,
    val dbFlag: Int,
) {

    TrueFalse("true-false", ActivityEntity.TYPE_TRUE_FALSE),
    Choice("choice", ActivityEntity.TYPE_CHOICE),
    FillIn("fill-in", ActivityEntity.TYPE_FILL_IN),
    LongFillIn("long-fill-in", ActivityEntity.TYPE_LONG_FILL_IN),
    Matching("matching", ActivityEntity.TYPE_MATCHING),
    Performance("performance", ActivityEntity.TYPE_PERFORMANCE),
    Sequencing("sequencing", ActivityEntity.TYPE_SEQUENCING),
    Likert("likert", ActivityEntity.TYPE_LIKERT),
    Numeric("numeric", ActivityEntity.TYPE_NUMERIC),
    Other("other", ActivityEntity.TYPE_OTHER);

    companion object {
        fun fromJsonFieldValue(value: String): XapiInteractionType {
            return entries.firstOrNull { it.jsonFieldValue == value } ?: Other
        }

        @Suppress("unused")
        fun fromDbFlag(value: Int) : XapiInteractionType? {
            return entries.firstOrNull { it.dbFlag == value }
        }
    }


}

object XapiInteractionTypeSerializer: KSerializer<XapiInteractionType> {
    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): XapiInteractionType {
        return XapiInteractionType.fromJsonFieldValue(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: XapiInteractionType) {
        encoder.encodeString(value.jsonFieldValue)
    }

}
