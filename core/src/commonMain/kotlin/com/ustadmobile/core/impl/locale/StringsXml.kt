package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.util.ext.innerXml
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory

/**
 * This class allows string retrieval from Android style string resource xml files using an integer
 * id code.
 */
class StringsXml (
    xpp: XmlPullParser,
    xppFactory: XmlPullParserFactory,
    idMap: Map<String, Int>,
    val stringsXmlName: String = "",
    val fallback: StringsXml? = null,
    val commentsEnabled: Boolean = false,
    private val trackMessageIdOrder: Boolean = false
){

    private val messageMap: Map<Int, String>

    private val commentMap: Map<Int, String>

    /**
     * The list of message ids in the order in which they were read in the Strings xml file itself.
     * Normally this is not required. It can be useful when working with the strings xml files
     * themselves (e.g. RemainingStringsTool).
     */
    val messageIdOrder: List<Int>
        get() {
            if(!trackMessageIdOrder)
                throw IllegalStateException("StringsXml.messgaeIdOrder: trackMessageIdOrder was not enabled")

            return _messageIdOrder
        }

    private val _messageIdOrder: List<Int>

    init {
        val mutableMap = mutableMapOf<Int, String>()
        val mutableCommentMap = mutableMapOf<Int, String>()
        val mutableMessageIdList = mutableListOf<Int>()

        var nextEvt: Int
        var commentString = ""
        while(xpp.nextToken().also { nextEvt = it } != XmlPullParserConstants.END_DOCUMENT) {
            if(commentsEnabled && nextEvt == 9) {
                commentString += xpp.getText()
            }

            if(nextEvt != XmlPullParserConstants.START_TAG)
                continue

            val tagName = xpp.getName()
            if(tagName != "string")
                continue

            val stringName = xpp.getAttributeValue(null, "name")
                ?: throw IllegalArgumentException("string in xml $stringsXmlName has no name!")

            val stringValue = xpp.innerXml(xppFactory)
            val stringId = idMap[stringName] ?: continue
            if(trackMessageIdOrder)
                mutableMessageIdList += stringId

            mutableMap[stringId] = stringValue
            if(commentsEnabled && commentString != "") {
                mutableCommentMap[stringId] = commentString
                commentString = ""
            }
        }

        messageMap = mutableMap.toMap()
        commentMap = mutableCommentMap.toMap()
        _messageIdOrder = mutableMessageIdList.toList()
    }

    operator fun get(messageId: Int): String {
        return messageMap[messageId] ?: fallback?.get(messageId)
            ?: throw IllegalArgumentException("$stringsXmlName does not contain")
    }

    /**
     * If comments were enabled, this function will return the comment
     * for the given message id. This is not needed in normal usage, but
     * might be needed when processing files for translation tools etc.
     */
    fun getComment(messageId: Int): String? {
        if(!commentsEnabled)
            throw IllegalStateException("StringsXml.getComment: commentsEnabled = false")

        return commentMap[messageId]
    }

    fun getIdByString(str: String, ignoreCase: Boolean = false): Int {
        val strTrimmed = str.trim()
        return messageMap.entries.firstOrNull { it.value.equals(strTrimmed, ignoreCase) }?.key ?: -1
    }

}