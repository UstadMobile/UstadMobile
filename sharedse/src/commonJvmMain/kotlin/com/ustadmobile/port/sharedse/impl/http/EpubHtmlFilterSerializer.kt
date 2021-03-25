package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.util.UMUtil
import com.ustadmobile.port.sharedse.util.XmlPassThroughFilter
import com.ustadmobile.port.sharedse.util.passXmlThrough
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Performs some minor tweaks on HTML being served to enable EPUB pagination and handling html
 * autoplay:
 * - Add a script immediately after the body tag so that it can, if desired, apply columns style.
 * This is only applied to HTML which is a top level frame, allownig content in an iframe to work
 * as expected.
 * - Add a meta viewport tag  -
 *
 */
class EpubHtmlFilterSerializer(override val di: DI) : DIAware {

    var scriptSrcToAdd: String? = null

    private var `in`: InputStream? = null

    //add the script
    val output: ByteArray
        get() {
            val bout = ByteArrayOutputStream()
            val xs: XmlSerializer by di.instance()
            xs.setOutput(bout, "UTF-8")

            val xpp: XmlPullParser by di.instance()
            xpp.setInput(`in`!!, "UTF-8")

            xs.startDocument("UTF-8", false)
            var seenViewPort = false
            passXmlThrough(xpp, xs, arrayOf("script", "style", "title"), object : XmlPassThroughFilter {
                @Throws(IOException::class)
                override fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                    if (evtType == XmlPullParser.END_TAG && parser.getName() == "head" && !seenViewPort) {
                        serializer.startTag(parser.getNamespace(), "meta")
                        serializer.attribute(parser.getNamespace(), "name", "viewport")
                        serializer.attribute(parser.getNamespace(), "content",
                                "height=device-height, initial-scale=1,user-scalable=no")
                        serializer.endTag(parser.getNamespace(), "meta")

                        serializer.startTag(parser.getNamespace(), "style")
                        serializer.attribute(parser.getNamespace(), "type", "text/css")
                        serializer.text("""
                            img, video, audio {
                                max-width: 95% !important;
                            }

                            body {
                                margin: 8dp;
                                overflow-x: hidden;
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
            })

            xs.endDocument()
            bout.flush()
            return bout.toByteArray()
        }

    fun setIntput(`in`: InputStream) {
        this.`in` = `in`
    }


}
