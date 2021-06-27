package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.lib.util.ext.XmlSerializerFilter
import com.ustadmobile.lib.util.ext.serializeTo
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.io.InputStream
import org.xmlpull.v1.XmlPullParserFactory

/**
 * Performs some minor tweaks on HTML being served to enable EPUB pagination and handling html
 * autoplay:
 * - Add a script immediately after the body tag so that it can, if desired, apply columns style.
 * This is only applied to HTML which is a top level frame, allownig content in an iframe to work
 * as expected.
 * - Add a meta viewport tag  -
 * - Compute document height to be set to an iframe for smooth scrolling on a client
 *
 */
class EpubHtmlFilterSerializer(override val di: DI) : DIAware {

    var scriptSrcToAdd: String? = null

    var liveWebServer: Boolean = false

    private var `in`: InputStream? = null

    class EpubXmlSerializerFilter: XmlSerializerFilter {

        var seenViewPort = false

        //Flag which controls what to be added to the page depending on whether it is on mobile or live server
        var onLiveWebServer: Boolean = false

        override fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
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

                            body {
                                margin: 8dp;
                                overflow-x: hidden;
                                display: flex;
                                justify-content: center;
                            }
                        """.trimIndent())
                serializer.endTag(parser.getNamespace(), "style")
            }
            return true
        }

        override fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
            if(evtType == XmlPullParser.START_TAG && parser.getName() == "meta"
                && parser.getAttributeValue(null, "name") == "viewport") {
                seenViewPort = true
            }

            return true
        }
    }

    //add the script
    val output: ByteArray
        get() {
            val xppFactory = XmlPullParserFactory.newInstance().also {
                it.isNamespaceAware = true
            }

            val bout = ByteArrayOutputStream()
            val xs: XmlSerializer = xppFactory.newSerializer()
            xs.setOutput(bout, "UTF-8")

            val xpp: XmlPullParser = xppFactory.newPullParser()
            xpp.setInput(`in`, "UTF-8")

            xpp.serializeTo(xs, inclusive = true, filter = EpubXmlSerializerFilter().apply {
                onLiveWebServer = liveWebServer
            })

            bout.flush()
            return bout.toByteArray()
        }

    fun setIntput(`in`: InputStream) {
        this.`in` = `in`
    }


}
