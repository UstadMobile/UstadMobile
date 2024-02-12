package com.ustadmobile.lib.util.ext

import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants.PROPERTY_STANDALONE
import com.ustadmobile.xmlpullparserkmp.XmlSerializer

/**
 * These are elements that must always have a separate start and end tag (even when they have
 * no content). Using a single trailing slash to end the tag will confuse browsers.
 */
private val SEPARATE_END_TAG_REQUIRED_ELEMENTS = arrayOf("script", "style", "title")

/**
 * Implement this interface to control some of the passXmlThrough methods .  This can be used
 * to add extra output to be serialized or to stop processing.
 */
interface XmlSerializerFilter {

    /**
     * Called before the given event is passed through to the XmlSerializer.
     *
     * @param evtType The event type from the parser
     * @param parser The XmlPullParser being used
     * @param serializer The XmlSerializer being used
     *
     * @return true to continue processing, false to end processing
     */
    fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

    /**
     * Called after the given event was passed through to the XmlSerializer.
     *
     * @param evtType The event type from the parser
     * @param parser The XmlPullParser being used
     * @param serializer The XmlSerializer being used
     *
     * @return true to continue processing, false to end processing
     */
    fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

}

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
                    getProperty(PROPERTY_STANDALONE) as? Boolean ?: false)
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
