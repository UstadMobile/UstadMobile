/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.lib.util


import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import kotlin.jvm.JvmOverloads
import org.xmlpull.v1.XmlSerializer

/**
 * Misc utility methods
 *
 * @author mike
 */
object UMUtil {

    /**
     * A list of elements that must have their own end tag
     */
    val SEPARATE_END_TAG_REQUIRED_ELEMENTS = arrayOf("script", "style", "title")


    /**
     * If i < 0  - return "0i", else return "i" - E.g. to dislpay 10:01 instead of 10:1
     * @param i Numbr to format
     * @return Number with leading 0 if it's less than 10
     */
    fun pad0(i: Int): String {
        return if (i > 9) {
            i.toString()
        } else {
            "0$i"
        }
    }

    /**
     * Get the index of an item in an array. Filler method because this doesn't existing on J2ME.
     *
     * @param haystack Array to search in
     * @param needle Value to search for
     * @param from Index to start searching from (inclusive)
     * @param to Index to search until (exclusive)
     * @return Index of needle in haystack, -1 if not found
     */
    @JvmOverloads
    fun indexInArray(haystack: Array<Any>, needle: Any?, from: Int = 0, to: Int = haystack.size): Int {
        for (i in from until to) {
            if (haystack[i] == needle) {
                return i
            }
        }

        return -1
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer.
     *
     * @param parser XmlPullParser XML is coming from
     * @param serializer XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     * required e.g. script, style etc. using &lt;/script&gt; instead
     * of &lt;script ... /&gt;
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    fun passXmlThrough(parser: XmlPullParser, serializer: XmlSerializer,
                       seperateEndTagRequiredElements: Array<String>) {
        val filter: PassXmlThroughFilter? = null
        passXmlThrough(parser, serializer, seperateEndTagRequiredElements, filter)
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer.
     *
     * @param parser XmlPullParser XML is coming from
     * @param serializer XmlSerializer XML is being written to
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    fun passXmlThrough(parser: XmlPullParser, serializer: XmlSerializer) {
        val filter: PassXmlThroughFilter? = null
        passXmlThrough(parser, serializer, null, filter)
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer.
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param separateHtmlEndTagRequiredElements if true then use the default list of html elements
     * that require a separate ending tag e.g. use
     * &lt;/script&gt; instead of &lt;script ... /&gt;
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
                       separateHtmlEndTagRequiredElements: Boolean,
                       filter: PassXmlThroughFilter
    ) {
        passXmlThrough(xpp, xs,
                if (separateHtmlEndTagRequiredElements) SEPARATE_END_TAG_REQUIRED_ELEMENTS else null, filter)
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer. This will not call startDocument
     * or endDocument - that must be called separately. This allows different documents to be merged.
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     * required e.g. script, style etc. using &lt;/script&gt; instead
     * of &lt;script ... /&gt;
     * @param filter XmlPassThroughFilter that can be used to add to output or interrupt processing
     * @throws XmlPullParserException
     * @throws IOException
     */
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
                       seperateEndTagRequiredElements: Array<String>?,
                       filter: PassXmlThroughFilter?) {

        var evtType = xpp.getEventType()
        var lastEvent = -1
        var tagName: String
        while (evtType != XmlPullParserConstants.END_DOCUMENT) {
            if (filter != null && !filter.beforePassthrough(evtType, xpp, xs))
                return

            when (evtType) {
                XmlPullParserConstants.DOCDECL -> {
                    xpp.getText()?.let {
                        xs.docdecl(it)
                    }
                }

                XmlPullParserConstants.START_TAG -> {
                    xs.startTag(xpp.getNamespace(), xpp.getName().toString())
                    for (i in 0 until xpp.getAttributeCount()) {
                        xs.attribute(xpp.getAttributeNamespace(i),
                                xpp.getAttributeName(i).toString(), xpp.getAttributeValue(i).toString())
                    }
                }
                XmlPullParserConstants.TEXT -> xs.text(xpp.getText().toString())
                XmlPullParserConstants.END_TAG -> {
                    tagName = xpp.getName().toString()

                    val haystack = seperateEndTagRequiredElements
                    if (lastEvent == XmlPullParserConstants.START_TAG
                            && seperateEndTagRequiredElements != null
                            && indexInArray(haystack as Array<Any>, tagName) != -1) {
                        xs.text(" ")
                    }

                    xs.endTag(xpp.getNamespace(), tagName)
                }
            }
            if (filter != null && !filter.afterPassthrough(evtType, xpp, xs))
                return

            lastEvent = evtType
            evtType = xpp.next()
        }
    }

    /**
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     * required e.g. script, style etc. using &lt;/script&gt; instead
     * of &lt;script ... /&gt;
     * @param endTagName If the given endTagName is encountered processing will stop. The endTag
     * for this tag will not be sent to the serializer.
     * @throws XmlPullParserException
     * @throws IOException
     */
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
                       seperateEndTagRequiredElements: Array<String>?,
                       endTagName: String) {

        passXmlThrough(xpp, xs, seperateEndTagRequiredElements, object : PassXmlThroughFilter {

            override fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                return !(evtType == XmlPullParserConstants.END_TAG && parser.getName() != null
                        && parser.getName() == endTagName)
            }

            override fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                return true
            }
        })
    }

    /**
     * Implement this interface to control some of the passXmlThrough methods .  This can be used
     * to add extra output to be serialized or to stop processing.
     */
    interface PassXmlThroughFilter {

        /**
         * Called before the given event is passed through to the XmlSerializer.
         *
         * @param evtType The event type from the parser
         * @param parser The XmlPullParser being used
         * @param serializer The XmlSerializer being used
         *
         * @return true to continue processing, false to end processing
         * @throws IOException
         * @throws XmlPullParserException
         */
        fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

        /**
         * Called after the given event was passed through to the XmlSerializer.
         *
         * @param evtType The event type from the parser
         * @param parser The XmlPullParser being used
         * @param serializer The XmlSerializer being used
         *
         * @return true to continue processing, false to end processing
         * @throws IOException
         * @throws XmlPullParserException
         */
        fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

    }

}
