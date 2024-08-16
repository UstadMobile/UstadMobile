package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
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

val XapiObjectType.typeFlag: Int
    get() = when(this) {
        XapiObjectType.StatementRef -> XapiEntityObjectTypeFlags.STATEMENT_REF
        XapiObjectType.SubStatement -> XapiEntityObjectTypeFlags.SUBSTATEMENT
        XapiObjectType.Activity -> XapiEntityObjectTypeFlags.ACTIVITY
        XapiObjectType.Agent -> XapiEntityObjectTypeFlags.AGENT
        XapiObjectType.Group -> XapiEntityObjectTypeFlags.GROUP
        XapiObjectType.Statement -> XapiEntityObjectTypeFlags.STATEMENT
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
