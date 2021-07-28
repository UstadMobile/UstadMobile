package com.ustadmobile.lib.rest.prototypestrings

import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.lib.util.ext.XmlSerializerFilter
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

/**
 * This is an XmlSerializerFilter that works for the PrototypeLocalizer to replace XML text
 * elements.
 */
class PrototypeLocalizerXmlFilter(val englishStrings: StringsXml,
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

            if(propertyName == "label" || propertyName == "textContent" || propertyName == "labelText") {
                inTextSection = true
            }

            return true
        }else if(parser.eventType == XmlPullParser.START_TAG &&
            (parser.name == "div" || parser.name == "tspan" || parser.name == "text")) {
            inTextSection = true
        }

        if(parser.eventType == XmlPullParser.END_TAG) {
            inTextSection = false
        }

        if((parser.eventType == XmlPullParser.CDSECT || parser.eventType == XmlPullParser.TEXT) &&
            inTextSection) {

            val text: String = parser.text
            val messageId = englishStrings.getIdByString(text, ignoreCase = true)


            //Text can be found in a CDSECT or plain text section.
            fun XmlSerializer.cdSectOrText(writeText: String) {
                if(parser.eventType == XmlPullParser.CDSECT) {
                    cdsect(writeText)
                }else {
                    text(writeText)
                }
            }

            if(messageId != -1) {
                serializer.cdSectOrText(foreignStrings[messageId])
                return false
            }else {
                //check and see if this is a compilation of something in brackets
                // e.g. Field name (optional) etc.
                val bracketEndings = listOf("(optional)", "(ascending)", "(descending)")
                if(bracketEndings.any { text.endsWith(it, ignoreCase = true) }) {
                    val splitIndex = text.lastIndexOf("(")
                    val prefix = text.substring(0, splitIndex).trim()
                    val prefixMessageId = englishStrings.getIdByString(prefix)

                    if(prefixMessageId != -1) {
                        val postfix = text.substring(splitIndex + 1)
                            .replace(")", "")
                        val postfixMessageId = englishStrings.getIdByString(postfix)

                        val postfixTranslated = if(postfixMessageId != -1)
                            foreignStrings[postfixMessageId]
                        else
                            ""
                        serializer.cdSectOrText(foreignStrings[prefixMessageId] + " " + postfixTranslated)
                        return false
                    }
                }


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