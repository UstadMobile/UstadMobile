package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument.Companion.NAMESPACE_XHTML
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "body",
    namespace = NAMESPACE_XHTML
)
@Serializable(with = BodySerializer::class)
class Body(
    val navigationElements: List<NavElement>,
)

/**
 * The navigation elements might not be direct descendants of the body tag. This custom serializer
 * handles that and ignores other contains that might be in the way.
 */
object BodySerializer: KSerializer<Body> {

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("body")

    override fun deserialize(decoder: Decoder): Body {
        return if(decoder is XML.XmlInput) {
            deserializeDynamic(decoder, decoder.input)
        }else {
            decoder.decodeStructure(descriptor) {
                decodeSerializableElement(descriptor, 0, Body.serializer())
            }
        }
    }


    //Nav elements might not be direct children
    //As per https://github.com/pdvrieze/xmlutil/blob/master/examples/DYNAMIC_TAG_NAMES.md
    fun deserializeDynamic(decoder: Decoder, reader: XmlReader) : Body {
        val xml = delegateFormat(decoder) // get the format for deserializing

        val navElementList = mutableListOf<NavElement>()

        decoder.decodeStructure(descriptor) {
            while (
                !(reader.next() == EventType.END_ELEMENT && reader.name.getLocalPart() == "body")
            ) {
                if(reader.eventType == EventType.START_ELEMENT && reader.localName == "nav") {
                    navElementList += xml.decodeFromReader(NavElement.serializer(), reader)
                }
            }
        }

        return Body(navigationElements = navElementList.toList())
    }

    override fun serialize(encoder: Encoder, value: Body) {
        encoder.encodeSerializableValue(Body.serializer(), value)
    }

    fun delegateFormat(decoder: Decoder) = (decoder as XML.XmlInput).delegateFormat()
}