package com.ustadmobile.core.api.oneroster.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Guid type enumeration as per OneRoster spec 4.14.2
 */
@kotlinx.serialization.Serializable(with = GuidRefTypeSerializer::class)
enum class GuidRefType {

    academicSession, category, clazz, course, demographics, enrollment, gradingPeriod, lineItem, org,
    resource, result, student, teacher, term, user

}

object GuidRefTypeSerializer: KSerializer<GuidRefType> {
    override fun deserialize(decoder: Decoder): GuidRefType {
        return GuidRefType.valueOf(decoder.decodeString())
    }


    override fun serialize(encoder: Encoder, value: GuidRefType) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

}