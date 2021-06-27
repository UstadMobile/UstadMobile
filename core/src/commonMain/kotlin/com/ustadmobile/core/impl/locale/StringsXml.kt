package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.util.ext.innerXml
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory

/**
 * This class allows string retrieval from Android style string resource xml files using an integer
 * id code.
 */
class StringsXml (xpp: XmlPullParser,
                  xppFactory: XmlPullParserFactory,
                  idMap: Map<String, Int>,
                  val stringsXmlName: String = "",
                  val fallback: StringsXml? = null){

    private val messageMap: Map<Int, String>

    init {
        val mutableMap = mutableMapOf<Int, String>()

        var nextEvt: Int
        while(xpp.next().also { nextEvt = it } != XmlPullParserConstants.END_DOCUMENT) {
            if(nextEvt != XmlPullParserConstants.START_TAG)
                continue

            val tagName = xpp.getName()
            if(tagName != "string")
                continue

            val stringName = xpp.getAttributeValue(null, "name")
                ?: throw IllegalArgumentException("string in xml $stringsXmlName has no name!")

            val stringValue = xpp.innerXml(xppFactory)
            val stringId = idMap[stringName] ?: continue

            mutableMap[stringId] = stringValue
        }

        messageMap = mutableMap.toMap()
    }

    operator fun get(messageId: Int): String {
        return messageMap[messageId] ?: fallback?.get(messageId)
            ?: throw IllegalArgumentException("$stringsXmlName does not contain")
    }

    fun getIdByString(str: String, ignoreCase: Boolean = false): Int {
        val strTrimmed = str.trim()
        return messageMap.entries.firstOrNull { it.value.equals(strTrimmed, ignoreCase) }?.key ?: -1
    }

}