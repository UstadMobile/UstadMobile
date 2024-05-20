package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.interop.oneroster.model.GuidRefType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = XapiObjectTypeSerializer::class)
enum class XapiObjectType {
    StatementRef, SubStatement, Activity, Agent, Group, Statement
}

object XapiObjectTypeSerializer: KSerializer<XapiObjectType> {

    override val descriptor: SerialDescriptor
        get() = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): XapiObjectType {
        val strValue = decoder.decodeString()
        return XapiObjectType.valueOf(strValue)
    }

    override fun serialize(encoder: Encoder, value: XapiObjectType) {
        encoder.encodeString(value.toString())
    }

}
