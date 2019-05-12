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

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.Vector

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */

/* $endif$ */

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
    val SEPARATE_END_TAG_REQUIRED_ELEMENTS = arrayOf("script", "style")


    /**
     * Gets the index of a particular item in an array
     *
     * Needed because J2ME does not support the Collections framework
     *
     * @param obj
     * @param arr
     * @return
     */
    fun getIndexInArray(obj: Any, arr: Array<Any>): Int {
        for (i in arr.indices) {
            if (arr[i] == obj) {
                return i
            }
        }

        return -1
    }

    /**
     * Find the index of a string in an array of strings using
     * equalsIgnoreCase to find the match
     *
     * @param str String to locate array
     * @param arr Array in which to search
     * @return the index of the given string in the array if found; -1 otherwise
     */
    fun getIndexInArrayIgnoreCase(str: String, arr: Array<String>): Int {
        for (i in arr.indices) {
            if (arr[i].equals(str, ignoreCase = true)) {
                return i
            }
        }

        return -1
    }

    /**
     * Tokenize the given string into a vector with String elements
     *
     * @param str the string to tokenize
     * @param deliminators the characters that are to be used as deliminators
     * @param start the position to start at (inclusive)
     * @pparam end the position to end at (exclusive)
     *
     * @return A vector with the string tokens as elements in the order in which they were found
     */
    fun tokenize(str: String, deliminators: CharArray, start: Int, end: Int): Vector<*> {
        var inToken = false
        var isDelim: Boolean

        var c: Char
        var i: Int = start
        var j: Int
        val tokens = Vector<String>()
        var tStart = 0


        while (i < end) {
            c = str[i]
            isDelim = false

            j = 0
            while (j < deliminators.size) {
                if (c == deliminators[j]) {
                    isDelim = true
                    break
                }
                j++
            }

            if (!isDelim && !inToken) {
                tStart = i
                inToken = true
            } else if (inToken && (isDelim || i == end - 1)) {
                tokens.addElement(str.substring(tStart, if (isDelim) i else i + 1))
                inToken = false
            }
            i++

        }

        return tokens
    }

    fun filterArrByPrefix(arr: Array<String>, prefix: String): Array<String?> {
        val matches = BooleanArray(arr.size)

        var i: Int = 0
        var matchCount = 0
        val arrayLen = arr.size

        while (i < arrayLen) {
            if (arr[i].startsWith(prefix)) {
                matches[i] = true
                matchCount++
            }
            i++
        }

        val retVal = arrayOfNulls<String>(matchCount)
        matchCount = 0
        i = 0
        while (i < arrayLen) {
            if (matches[i]) {
                retVal[matchCount] = arr[i]
                matchCount++
            }
            i++
        }

        return retVal
    }

    /**
     * Utility method to fill boolean array with a set value
     *
     * @param arr The boolean array
     * @param value Value to put in
     * @param from starting index (inclusive)
     * @param to  end index (exclusive)
     */
    fun fillBooleanArray(arr: BooleanArray, value: Boolean, from: Int, to: Int) {
        for (i in from until to) {
            arr[i] = value
        }
    }

    /**
     * Convert an enumeration into an array of Strings (where each element in
     * the enumeration is a string)
     *
     * @param enu Enumeration to convert
     * @return String array
     */
    fun enumerationToStringArray(enu: Enumeration<Any>): MutableList<Int> {
        val listVector = Vector<String>()
        while (enu.hasMoreElements()) {
            listVector.addElement(enu.nextElement() as String?)
        }
        val list = mutableListOf(listVector.size)
        listVector.copyInto(list.toTypedArray())
        return list
    }

    /**
     * Add all elements from an enumeration to the given vector
     *
     * @param e Enumeration
     * @param v Vector
     * @return The same vector as was given as an argument
     */
    fun addEnumerationToVector(e: Enumeration<Any>, v: Vector<Any>): Vector<*> {
        while (e.hasMoreElements()) {
            v.addElement(e.nextElement())
        }
        return v
    }

    /**
     * Add all the elements from one vector to another
     *
     * @param vector The vector elements will be added to
     * @param toAdd The vector elements will be added from
     * @return the vector with all the elements (same as vector argument)
     */
    fun addVectorToVector(vector: Vector<Any>, toAdd: Vector<Any>): Vector<Any> {
        val numElements = toAdd.size
        vector.ensureCapacity(vector.size + toAdd.size)
        for (i in 0 until numElements) {
            vector.addElement(toAdd.elementAt(i))
        }

        return vector
    }

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
     * Copy references from one hashtable to another hashtable
     *
     * @param src Source hashtable to copy from
     * @param dst Destination hashtable to copy into
     */
    fun copyHashtable(src: Map<String, String>, dst: MutableMap<String, String?>) {
        val keys = src.keys.iterator()
        var key: Any
        while (keys.hasNext()) {
            key = keys.next()
            dst[key] = src[key]
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
    @Throws(XmlPullParserException::class, IOException::class)
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
    @Throws(XmlPullParserException::class, IOException::class)
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
    @Throws(XmlPullParserException::class, IOException::class)
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
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
    @Throws(XmlPullParserException::class, IOException::class)
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
                       seperateEndTagRequiredElements: Array<String>?,
                       filter: PassXmlThroughFilter?) {

        var evtType = xpp.eventType
        var lastEvent = -1
        var tagName: String
        while (evtType != XmlPullParser.END_DOCUMENT) {
            if (filter != null && !filter.beforePassthrough(evtType, xpp, xs))
                return

            when (evtType) {
                XmlPullParser.START_TAG -> {
                    xs.startTag(xpp.namespace, xpp.name)
                    for (i in 0 until xpp.attributeCount) {
                        xs.attribute(xpp.getAttributeNamespace(i),
                                xpp.getAttributeName(i), xpp.getAttributeValue(i))
                    }
                }
                XmlPullParser.TEXT -> xs.text(xpp.text)
                XmlPullParser.END_TAG -> {
                    tagName = xpp.name

                    val haystack = seperateEndTagRequiredElements
                    if (lastEvent == XmlPullParser.START_TAG
                            && seperateEndTagRequiredElements != null
                            && indexInArray(haystack as Array<Any>, tagName) != -1) {
                        xs.text(" ")
                    }

                    xs.endTag(xpp.namespace, tagName)
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
    @Throws(XmlPullParserException::class, IOException::class)
    fun passXmlThrough(xpp: XmlPullParser, xs: XmlSerializer,
                       seperateEndTagRequiredElements: Array<String>?,
                       endTagName: String) {
        passXmlThrough(xpp, xs, seperateEndTagRequiredElements, object : PassXmlThroughFilter {
            @Throws(IOException::class, XmlPullParserException::class)
            override fun beforePassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean {
                return !(evtType == XmlPullParser.END_TAG && parser.name != null
                        && parser.name == endTagName)
            }

            @Throws(IOException::class, XmlPullParserException::class)
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
        @Throws(IOException::class, XmlPullParserException::class)
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
        @Throws(IOException::class, XmlPullParserException::class)
        fun afterPassthrough(evtType: Int, parser: XmlPullParser, serializer: XmlSerializer): Boolean

    }


    @Throws(XmlPullParserException::class, IOException::class)
    fun passXmlThroughToString(xpp: XmlPullParser, endTagName: String, xs: XmlSerializer): String {
        val bout = ByteArrayOutputStream()
        xs.setOutput(bout, "UTF-8")
        xs.startDocument("UTF-8", java.lang.Boolean.FALSE)
        passXmlThrough(xpp, xs, null, endTagName)
        xs.endDocument()
        bout.flush()
        return String(bout.toByteArray())
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
        val buffer = StringBuffer()
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
    fun joinStrings(objects: Vector<*>, joiner: String): String {
        val buffer = StringBuffer()
        for (i in objects.indices) {
            buffer.append(objects.elementAt(i))

            if (i < objects.size - 1)
                buffer.append(joiner)
        }

        return buffer.toString()
    }


    /**
     * Encode a username and password as a basic auth header
     * @param username
     * @param password
     * @return
     */
    fun encodeBasicAuth(username: String, password: String): String {
        return "Basic " + Base64Coder.encodeToString(username +
                ':'.toString() + password)
    }


}
/**
 * Get the index of an item in an array. Filler method because this doesn't existing on J2ME.
 *
 * @param haystack Array to search in
 * @param needle Value to search for
 */
