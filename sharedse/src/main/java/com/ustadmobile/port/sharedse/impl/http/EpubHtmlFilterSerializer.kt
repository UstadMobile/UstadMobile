package com.ustadmobile.port.sharedse.impl.http

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.util.UMUtil

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
class EpubHtmlFilterSerializer {

    var scriptSrcToAdd: String? = null

    private var `in`: InputStream? = null

    //add the script
    val output: ByteArray
        @Throws(IOException::class, XmlPullParserException::class)
        get() {
            val bout = ByteArrayOutputStream()
            val xs = UstadMobileSystemImpl.instance.newXMLSerializer()
            xs.setOutput(bout, "UTF-8")

            val xpp = UstadMobileSystemImpl.instance.newPullParser(`in`!!, "UTF-8")
            xs.startDocument("UTF-8", false)
            UMUtil.passXmlThrough(xpp, xs, true, object : UMUtil.PassXmlThroughFilter {
                @Throws(IOException::class)
                fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                    if (evtType == XmlPullParser.END_TAG && parser.name == "head") {
                        serializer.startTag(parser.namespace, "meta")
                        serializer.attribute(parser.namespace, "name", "viewport")
                        serializer.attribute(parser.namespace, "content",
                                "height=device-height, initial-scale=1,user-scalable=no")
                        serializer.endTag(parser.namespace, "meta")
                    }
                    return true
                }

                @Throws(IOException::class, XmlPullParserException::class)
                fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                    if (evtType == XmlPullParser.START_TAG && parser.name == "body") {
                        serializer.startTag(parser.namespace, "script")
                        serializer.attribute(parser.namespace, "src", scriptSrcToAdd)
                        serializer.attribute(parser.namespace, "type", "text/javascript")
                        serializer.text(" ")
                        serializer.endTag(parser.namespace, "script")
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
