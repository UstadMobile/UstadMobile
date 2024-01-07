package com.ustadmobile.core.util.stringvalues

import com.ustadmobile.core.util.ext.toMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Kotlinx Serializer for StringValues (delegate serializer using map)
 */
class StringValuesSerializer : KSerializer<IStringValues> {

    private val delegateSerializer = MapSerializer(
        String.serializer(), ListSerializer(String.serializer())
    )


    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("StringValuesSerializer",
        delegateSerializer.descriptor)


    override fun deserialize(decoder: Decoder): IStringValues {
        val stringMap = decoder.decodeSerializableValue(delegateSerializer)
        return MapStringValues(stringMap)
    }

    override fun serialize(encoder: Encoder, value: IStringValues) {
        encoder.encodeSerializableValue(delegateSerializer, value.toMap())
    }
}