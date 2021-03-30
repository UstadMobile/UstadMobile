package com.ustadmobile.core.util

import com.ustadmobile.xmlpullparserkmp.XmlPullParser

/**
 * Presenters need to be able to access an XmlPullParser using KodeIn DI from Kotlin shared (common)
 * code. On Android and JVM the most efficient approach is to read directly from a stream from
 * a file or URL (e.g. without buffering the entire contents).
 *
 * However the XmlPullParser interface itself does not define a function to close the inputStream
 * it is reading from. This interface can be used by multiplatform code to provide a 'handle' for
 * such a situation, so it can be instructed to close.
 */
interface XmlPullParserHandle {

    val xmlPullParser: XmlPullParser

    fun close()

}