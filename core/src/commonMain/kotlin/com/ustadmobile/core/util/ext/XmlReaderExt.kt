package com.ustadmobile.core.util.ext

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader

/**
 * Read through XHTML and convert the content to plain text. This will append all text sections and
 * include the alt/title content of any img tags.
 */
fun XmlReader.xhtmlContentToText(): String = buildString {
    val initTagName = name
    val initDepth = depth

    while(!(
        next() == EventType.END_ELEMENT &&
            depth == (initDepth - 1) && //Once we hit the end tag, the depth will be one lower
            name == initTagName
    )) {
        when(eventType) {
            EventType.START_ELEMENT -> {
                if(name.getLocalPart() == "img") {
                    val textVal = getAttributeValue(nsUri = null, localName = "alt")
                        ?: getAttributeValue(nsUri = null, localName = "title")
                    textVal?.also {
                        append(it)
                    }
                }
            }
            EventType.TEXT -> {
                append(text.trimExcessWhiteSpace())
            }
            else -> {
                //do nothing
            }
        }
    }

}