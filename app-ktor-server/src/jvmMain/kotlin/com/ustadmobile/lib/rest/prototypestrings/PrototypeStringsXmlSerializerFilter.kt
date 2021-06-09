package com.ustadmobile.lib.rest.prototypestrings

import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.lib.util.ext.XmlSerializerFilter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import javax.management.modelmbean.XMLParseException

class PrototypeStringsXmlSerializerFilter(val englishStrings: StringsXml,
                                          val foreignStrings: StringsXml): XmlSerializerFilter {

    var inTextSection: Boolean = false

    var inNameText: Boolean = false

    private val notFoundList = mutableSetOf<String>()

    var pageName: String? = null

    val stringsNotFound: Set<String>
        get() = notFoundList.toSet()

    override fun beforePassthrough(
        evtType: Int,
        parser: XmlPullParser,
        serializer: XmlSerializer
    ): Boolean {
        if(parser.eventType == XmlPullParser.START_TAG &&
            parser.name.equals("property", ignoreCase = true)) {

            val propertyName = parser.getAttributeValue(null, "name")

            if(propertyName == "name") {
                inNameText = true
            }
        }

        if(parser.eventType == XmlPullParser.START_TAG && parser.name == "property") {
            val propertyName = parser.getAttributeValue(null, "name")

            if(propertyName == "label" || propertyName == "textContent") {
                inTextSection = true
            }

            return true
        }else if(parser.eventType == XmlPullParser.START_TAG && parser.name == "div") {
            val propertyName = parser.getAttributeValue("http://www.evolus.vn/Namespace/Pencil",
                "name")

            if(propertyName == "text")
                inTextSection = true
        }

        if(parser.eventType == XmlPullParser.END_TAG) {
            inTextSection = false
        }

        if(parser.eventType == XmlPullParser.CDSECT && inTextSection) {
            val text: String = parser.text
            val messageId = englishStrings.getIdByString(text, ignoreCase = true)
            if(messageId != -1) {
                serializer.cdsect(foreignStrings[messageId])
                return false
            }else {
                notFoundList.takeIf { text.isNotBlank() }?.add(text)
                return true
            }
        }

        if(parser.eventType == XmlPullParser.TEXT && inNameText) {
            pageName = parser.text
            inNameText = false
        }

        return true
    }

    override fun afterPassthrough(
        evtType: Int,
        parser: XmlPullParser,
        serializer: XmlSerializer
    ): Boolean {
        return true
    }
}