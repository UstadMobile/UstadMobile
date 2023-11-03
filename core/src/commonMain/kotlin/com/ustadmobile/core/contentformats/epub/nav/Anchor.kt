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

/**
 * As per the EPUB navigation document specification, the anchor and span elements can include
 * valid XHTML elements (e.g. span, img etc). Readers can convert this content to text. This
 * will include the alt text description for images.
 */
@Serializable(
    with = AnchorSerializer::class
)
@XmlSerialName(
    value = "a",
    namespace = NAMESPACE_XHTML,
)
class Anchor(

    @XmlValue
    val content: String = "",

    val href: String = "",

)


//See https://github.com/pdvrieze/xmlutil/blob/master/examples/DYNAMIC_TAG_NAMES.md
object AnchorSerializer: KSerializer<Anchor> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("a")

    override fun deserialize(decoder: Decoder): Anchor {
        return if(decoder is XML.XmlInput) {
            deserializeDynamic(decoder.input)
        }else {
            decoder.decodeStructure(descriptor) {
                decodeSerializableElement(descriptor, 0, Anchor.serializer())
            }
        }
    }

    private fun deserializeDynamic(reader: XmlReader): Anchor {
        val href = reader.getAttributeValue(
            nsUri = NAMESPACE_XHTML,
            localName = "href"
        )
        val text = reader.xhtmlContentToText().trim()

        return Anchor(content = text, href = href ?: "")
    }

    override fun serialize(encoder: Encoder, value: Anchor) {
        encoder.encodeSerializableValue(Anchor.serializer(), value)
    }
}
