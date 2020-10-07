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

import org.kmp.io.KMPPullParser
import org.kmp.io.KMPSerializerParser
import org.kmp.io.KMPXmlParser
import kotlin.js.JsName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * Misc utility methods
 *
 * @author mike
 */
object UMUtil {

    val PORT_ALLOC_IO_ERR = -1

    val PORT_ALLOC_SECURITY_ERR = -2

    val PORT_ALLOC_OTHER_ERR = 3

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
    fun passXmlThrough(parser: KMPXmlParser, serializer: KMPSerializerParser,
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
    fun passXmlThrough(parser: KMPXmlParser, serializer: KMPSerializerParser) {
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
    fun passXmlThrough(xpp: KMPXmlParser, xs: KMPSerializerParser,
                       separateHtmlEndTagRequiredElements: Boolean,
                       filter: PassXmlThroughFilter) {
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
    fun passXmlThrough(xpp: KMPXmlParser, xs: KMPSerializerParser,
                       seperateEndTagRequiredElements: Array<String>?,
                       filter: PassXmlThroughFilter?) {

        var evtType = xpp.getEventType()
        var lastEvent = -1
        var tagName: String
        while (evtType != KMPPullParser.END_DOCUMENT) {
            if (filter != null && !filter.beforePassthrough(evtType, xpp, xs))
                return

            when (evtType) {
                KMPPullParser.DOCDECL -> {
                    xpp.getText()?.let {
                        xs.docdecl(it)
                    }
                }

                KMPPullParser.START_TAG -> {
                    xs.startTag(xpp.getNamespace(), xpp.getName().toString())
                    for (i in 0 until xpp.getAttributeCount()) {
                        xs.attribute(xpp.getAttributeNamespace(i),
                                xpp.getAttributeName(i).toString(), xpp.getAttributeValue(i).toString())
                    }
                }
                KMPPullParser.TEXT -> xs.text(xpp.getText().toString())
                KMPPullParser.END_TAG -> {
                    tagName = xpp.getName().toString()

                    val haystack = seperateEndTagRequiredElements
                    if (lastEvent == KMPPullParser.START_TAG
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
    fun passXmlThrough(xpp: KMPXmlParser, xs: KMPSerializerParser,
                       seperateEndTagRequiredElements: Array<String>?,
                       endTagName: String) {
        passXmlThrough(xpp, xs, seperateEndTagRequiredElements, object : PassXmlThroughFilter {

            override fun beforePassthrough(evtType: Int, parser: KMPXmlParser, serializer: KMPSerializerParser): Boolean {
                return !(evtType == KMPPullParser.END_TAG && parser.getName() != null
                        && parser.getName() == endTagName)
            }

            override fun afterPassthrough(evtType: Int, parser: KMPXmlParser, serializer: KMPSerializerParser): Boolean {
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
        fun beforePassthrough(evtType: Int, parser: KMPXmlParser, serializer: KMPSerializerParser): Boolean

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
        fun afterPassthrough(evtType: Int, parser: KMPXmlParser, serializer: KMPSerializerParser): Boolean

    }

    /**
     * Determine if the two given locales are the same language - e.g. en, en-US, en-GB etc.
     *
     * @param locale1
     * @param locale2
     *
     * @return True if the given locales are the same language, false otherwise
     */
    fun isSameLanguage(locale1: String?, locale2: String?): Boolean {
        return if (locale1 == null && locale2 == null) {
            true//no language to compare
        } else if (locale1 == null || locale2 == null) {
            false//one is null
        } else {
            locale1.substring(0, 2) == locale2.substring(0, 2)
        }
    }

    /**
     * Generate a new hashtable which is 'flipped' - e.g. where the keys of the input hashtable become
     * the values in the output hashtable, and vice versa.
     *
     * @return Flip hashtable
     */
    fun <V, K> flipMap(source: Map<K, V>, dest: MutableMap<V, K>): Map<V, K> {
        for ((key, value) in source) {
            dest[value] = key
        }

        return dest
    }

    /**
     * Joins strings e.g. from an array generate a single string with "Bob", "Anand", "Kate" etc.
     *
     * @param objects Objects to join - using toString method
     * @param joiner String to use between each object.
     *
     * @return A single string formed by each object's toString method, followed by the joiner, the
     * next object, and so on.
     */
    fun joinStrings(objects: Array<Any>, joiner: String): String {
        val buffer = StringBuilder()
        for (i in objects.indices) {
            buffer.append(objects.toString())

            if (i < objects.size - 1)
                buffer.append(joiner)
        }

        return buffer.toString()
    }

    fun joinStrings(strings: Iterable<*>, joiner: String): String {
        var isFirst = true
        val sb = StringBuilder()
        for (o in strings) {
            if (!isFirst)
                sb.append(joiner)

            sb.append(o.toString())
            isFirst = false
        }

        return sb.toString()
    }

    /**
     * Joins strings e.g. from an array generate a single string with "Bob", "Anand", "Kate" etc.
     *
     * @param objects Objects to join - using toString method
     * @param joiner String to use between each object.
     *
     * @return A single string formed by each object's toString method, followed by the joiner, the
     * next object, and so on.
     */
    fun joinStrings(objects: MutableList<*>, joiner: String): String {
        val buffer = StringBuilder()
        for (i in objects.indices) {
            buffer.append(objects.elementAt(i))

            if (i < objects.size - 1)
                buffer.append(joiner)
        }

        return buffer.toString()
    }


    /* /**
      * Encode a username and password as a basic auth header
      * @param username
      * @param password
      * @return
      */
     fun encodeBasicAuth(username: String, password: String): String {
         return "Basic " + Base64Coder.encodeToString(username +
                 ':'.toString() + password)
     } */


    @JsName("jsArrayToKotlinList")
    @JvmStatic
    fun jsArrayToKotlinList(array: Any): Any{
        val jsArray = array as Array<Any>
        return jsArray.toList()
    }

    @JsName("kotlinListToJsArray")
    @JvmStatic
    fun kotlinListToJsArray(list: ArrayList<Any>): Any{
        return list.toTypedArray()
    }

    @JsName("kotlinCategoryMapToJsArray")
    @JvmStatic
    fun kotlinCategoryMapToJsArray(categoriesMap: HashMap<Long, List<Any>>) : Array<Any>{
        val categories = mutableListOf<Any>()
        (ArrayList(categoriesMap.values)).forEach { list -> categories.add(list.toTypedArray()) }
        return categories.toTypedArray()
    }

    @JsName("kotlinMapToJsArray")
    @JvmStatic
    fun kotlinMapToJsArray(map: HashMap<String,String>): Any{
        val outList = mutableListOf<Any>()
        map.entries.forEach {
            outList.add(object {
                var key =  it.key
                var value = it.value})
        }
        return outList.toTypedArray()
    }


    @JvmStatic
    fun getH5pLicenceId(licence: String?): Int{
        when(licence){
            "cc-by" -> return 1
            "cc-by-sa" -> return 2
            "cc-by-nc" -> return 4
            "cc-by-nc-sa" -> return 6

        }
        return 8
    }

}
