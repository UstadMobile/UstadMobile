package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import com.ustadmobile.core.util.ext.xhtmlContentToText
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@XmlSerialName(
    value = "span",
    namespace = NAMESPACE_XHTML,
)
@Serializable(with = SpanSerializer::class)
class Span(
    @XmlValue
    val content: String = ""
)

object SpanSerializer: KSerializer<Span> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("span")


    override fun deserialize(decoder: Decoder): Span {
        return if(decoder is XML.XmlInput) {
            deserializeDynamic(decoder.input)
        }else {
            decoder.decodeStructure(AnchorSerializer.descriptor) {
                decodeSerializableElement(AnchorSerializer.descriptor, 0, Span.serializer())
            }
        }
    }

    private fun deserializeDynamic(reader: XmlReader): Span {
        val text = reader.xhtmlContentToText().trim()

        return Span(content = text)
    }

    override fun serialize(encoder: Encoder, value: Span) {
        encoder.encodeSerializableValue(Span.serializer(), value)
    }
}
