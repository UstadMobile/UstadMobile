package com.ustadmobile.core.util.xmlfilter

import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

val SEPARATE_END_TAG_REQUIRED_ELEMENTS = arrayOf("script", "style", "title")

/**
 * Serialize events from this XmlPullParser to an XmlSerializer. This can Serialize a whole document
 * or a subsection thereof. The filter can be used to manipulate the output as required (e.g. add
 * extra serialization output, skip output, etc).
 *
 * Where the XmlPullParser is currently on a START_TAG event, serialization will automatically stop
 * at the corresponding END_TAG event (which may be inclusive or exclusive as per the argument given).
 *
 * @param xmlSerializer serializer that events are written to
 * @param inclusive if true, and the pull parser is starting on a START_TAG event, then the START_TAG
 * and END_TAG for the given tag are passed to the serializer. Otherwise they are not passed (e.g.
 * the result is similar to innerHTML).
 * @param filter XmlSerializerFilter (optional)
 * @param separateEndTagRequiredElements a list of tag names which must have separate start and end
 * tags even when they are empty
 */
fun XmlPullParser.serializeTo(
    xmlSerializer: XmlSerializer,
    inclusive: Boolean = true,
    filter: XmlSerializerFilter? = null,
    separateEndTagRequiredElements: Array<String> = SEPARATE_END_TAG_REQUIRED_ELEMENTS
) {

    var evtType = getEventType()
    var lastEvent = -1
    var tagName: String


    var endTagName: String? = null
    var endDepth: Int = -1
    if(evtType == XmlPullParserConstants.START_TAG) {
        endTagName = getName()
        endDepth = getDepth()
    }

    if(!inclusive)
        evtType = nextToken()

    do {
        if (filter != null && !filter.beforePassthrough(evtType, this, xmlSerializer)) {
            lastEvent = evtType
            evtType = nextToken()
            continue
        }

        when (evtType) {
            XmlPullParserConstants.DOCDECL -> {
                getText()?.let {
                    xmlSerializer.docdecl(it)
                }
            }

            XmlPullParserConstants.START_DOCUMENT -> {
                xmlSerializer.startDocument(getInputEncoding(),
                    getProperty(XmlPullParserConstants.PROPERTY_STANDALONE) as? Boolean ?: false)
            }

            XmlPullParserConstants.START_TAG -> {
                //pass through namespace prefixes
                for(i in getNamespaceCount(getDepth() - 1) until getNamespaceCount(getDepth())) {
                    val prefix = getNamespacePrefix(i)
                    val nsUri = getNamespaceUri(i)
                    if(xmlSerializer.getPrefix(nsUri, false) == null){
                        xmlSerializer.setPrefix(prefix ?: "", nsUri)
                    }
                }


                xmlSerializer.startTag(getNamespace(), getName().toString())
                for (i in 0 until getAttributeCount()) {
                    xmlSerializer.attribute(
                        getAttributeNamespace(i),
                        getAttributeName(i), getAttributeValue(i)
                    )
                }
            }
            XmlPullParserConstants.TEXT -> {
                xmlSerializer.text(getText().toString())
            }

            XmlPullParserConstants.CDSECT -> {
                xmlSerializer.cdsect(getText().toString())
            }

            XmlPullParserConstants.END_TAG -> {
                tagName = getName().toString()

                val isEndTag = getDepth() == endDepth && tagName == endTagName
                if(isEndTag && !inclusive)
                    return

                if (lastEvent == XmlPullParserConstants.START_TAG
                    && tagName in separateEndTagRequiredElements
                ) {
                    xmlSerializer.text(" ")
                }

                xmlSerializer.endTag(getNamespace(), tagName)

                if(isEndTag)
                    return
            }
        }

        if (filter != null && !filter.afterPassthrough(evtType, this, xmlSerializer))
            return

        lastEvent = evtType
        evtType = nextToken()
    } while (evtType != XmlPullParserConstants.END_DOCUMENT)

    xmlSerializer.flush()
}
