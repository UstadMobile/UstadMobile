package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.util.ext.serializeTo
import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlPullParserFactory
import java.io.ByteArrayOutputStream

actual fun XmlPullParser.innerXml(xppFactory: XmlPullParserFactory): String {
    if(eventType != XmlPullParserConstants.START_TAG)
        throw IllegalStateException("innerXml only works on a START_TAG event!")

    val serializer = xppFactory.newSerializer()
    val byteArrayOut = ByteArrayOutputStream()
    serializer.setOutput(byteArrayOut, "utf-8")
    serializeTo(serializer, inclusive = false)
    serializer.flush()

    return String(byteArrayOut.toByteArray())
}
