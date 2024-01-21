package com.ustadmobile.core.util.xmlfilter

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

class EpubXmlSerializerFilter: XmlSerializerFilter {

    var seenViewPort = false

    //Flag which controls what to be added to the page depending on whether it is on mobile or live server
    var onLiveWebServer: Boolean = false

    override fun beforePassthrough(
        evtType: Int,
        parser: XmlPullParser,
        serializer: XmlSerializer
    ): Boolean {
        if (evtType == XmlPullParser.END_TAG && parser.getName() == "head" && !seenViewPort) {
            serializer.startTag(parser.getNamespace(), "meta")
            serializer.attribute("", "name", "viewport")
            serializer.attribute("", "content",
                "height=device-height, initial-scale=1,user-scalable=no")
            serializer.endTag(parser.getNamespace(), "meta")

            serializer.startTag(parser.getNamespace(), "style")
            serializer.attribute("", "type", "text/css")
            serializer.text("""
                img, video, audio {
                    max-width: ${if(onLiveWebServer) "100" else "95"}% !important;
                }
            """.trimIndent())
            serializer.endTag(parser.getNamespace(), "style")
        }
        return true
    }

    override fun afterPassthrough(
        evtType: Int,
        parser: XmlPullParser,
        serializer: XmlSerializer
    ): Boolean {
        if(evtType == XmlPullParser.START_TAG && parser.getName() == "meta"
            && parser.getAttributeValue(null, "name") == "viewport") {
            seenViewPort = true
        }

        return true
    }
}