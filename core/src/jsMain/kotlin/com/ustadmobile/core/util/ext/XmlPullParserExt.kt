package com.ustadmobile.core.util.ext

import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory

/**
 * This function is designed to return something similar to innerHTML on Javascript. This function
 * must be called on a START_TAG event. When it finishes, the XmlPullParser will be on the END_TAG
 * event.
 *
 * It has been
 * created as an expect/actual function. On Javascript a shortcut can be used: it can simply use
 * the innerHTML, and then skip
 */
actual fun XmlPullParser.innerXml(xppFactory: XmlPullParserFactory): String {
    if(getEventType() != XmlPullParserConstants.START_TAG)
       throw IllegalStateException("innerXml only works on a START_TAG event!")

    val value =  getNode()?.textContent?:""
    next()
    return value.subSequence(0,value.length - 4).toString()
}