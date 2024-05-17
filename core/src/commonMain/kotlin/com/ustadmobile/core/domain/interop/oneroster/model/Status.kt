package com.ustadmobile.core.domain.interop.oneroster.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@kotlinx.serialization.Serializable(with = StatusSerializer::class)
enum class Status {

    ACTIVE, TOBEDELETED;

    companion object {

        fun fromIsDeletedBool(isDeleted: Boolean) = if(isDeleted) TOBEDELETED else ACTIVE

    }

}

object StatusSerializer: KSerializer<Status> {
    override fun deserialize(decoder: Decoder): Status {
        return Status.valueOf(decoder.decodeString().uppercase())
    }

    override fun serialize(encoder: Encoder, value: Status) {
        encoder.encodeString(value.toString().lowercase())
    }

    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()
}
