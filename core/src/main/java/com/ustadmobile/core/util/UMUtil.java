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
package com.ustadmobile.core.util;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */

/* $endif$ */

/**
 * Misc utility methods
 * 
 * @author mike
 */
public class UMUtil {
    
    public static final int PORT_ALLOC_IO_ERR = -1;
    
    public static final int PORT_ALLOC_SECURITY_ERR = -2;
    
    public static final int PORT_ALLOC_OTHER_ERR = 3;

    /**
     * A list of elements that must have their own end tag
     */
    public static final String[] SEPARATE_END_TAG_REQUIRED_ELEMENTS = new String[] {"script", "style"};




    /**
     * Gets the index of a particular item in an array
     * 
     * Needed because J2ME does not support the Collections framework
     * 
     * @param obj
     * @param arr
     * @return 
     */
    public static final int getIndexInArray(Object obj, Object[] arr) {
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] != null && arr[i].equals(obj)) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Find the index of a string in an array of strings using
     * equalsIgnoreCase to find the match
     * 
     * @param str String to locate array
     * @param arr Array in which to search
     * @return the index of the given string in the array if found; -1 otherwise
     */
    public static final int getIndexInArrayIgnoreCase(String str, String[] arr) {
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] != null && arr[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        
        return -1;
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
    public static Vector tokenize(String str, char[] deliminators, int start, int end) {
        boolean inToken = false;
        boolean isDelim;
        
        char c;
        int i;
        int j;
        Vector tokens = new Vector();
        int tStart = 0;
        
        
        for(i = start; i < end; i++) {
            c = str.charAt(i);
            isDelim = false;
            
            for(j = 0; j < deliminators.length; j++) {
                if(c == deliminators[j]) {
                    isDelim = true;
                    break;
                }
            }
            
            if(!isDelim && !inToken) {
                tStart = i;
                inToken = true;
            }else if(inToken && (isDelim || i == end-1)) {
                tokens.addElement(str.substring(tStart, isDelim ? i : i+1));
                inToken = false;
            }
            
        }
        
        return tokens;
    }
    
    public static final String[] filterArrByPrefix(String[] arr, String prefix) {
        boolean[] matches = new boolean[arr.length];
        
        int i;
        int matchCount = 0;
        int arrayLen = arr.length;
        
        for(i = 0; i < arrayLen; i++) {
            if(arr[i] != null && arr[i].startsWith(prefix)) {
                matches[i] = true;
                matchCount++;
            }
        }
        
        String[] retVal = new String[matchCount];
        matchCount = 0;
        for(i = 0; i < arrayLen; i++) {
            if(matches[i]) {
                retVal[matchCount] = arr[i];
                matchCount++;
            }
        }
        
        return retVal;
    }
    
    /**
     * Utility method to fill boolean array with a set value
     * 
     * @param arr The boolean array
     * @param value Value to put in
     * @param from starting index (inclusive)
     * @param to  end index (exclusive)
     */
    public static void fillBooleanArray(boolean[] arr, boolean value, int from, int to) {
        for(int i = from; i < to; i++) {
            arr[i] = value;
        }
    }
    
    /**
     * Convert an enumeration into an array of Strings (where each element in
     * the enumeration is a string)
     * 
     * @param enu Enumeration to convert
     * @return String array
     */
    public static String[] enumerationToStringArray(Enumeration enu){
        String[] list;
        Vector listVector = new Vector();
        while(enu.hasMoreElements()){
            listVector.addElement(enu.nextElement());
        }
        list = new String[listVector.size()];
        listVector.copyInto(list);
        return list;
    }
    
    /**
     * Add all elements from an enumeration to the given vector
     * 
     * @param e Enumeration 
     * @param v Vector
     * @return The same vector as was given as an argument
     */
    public static Vector addEnumerationToVector(Enumeration e, Vector v) {
        while(e.hasMoreElements()) {
            v.addElement(e.nextElement());
        }
        return v;
    }
    
    /**
     * Add all the elements from one vector to another
     * 
     * @param vector The vector elements will be added to
     * @param toAdd The vector elements will be added from
     * @return the vector with all the elements (same as vector argument)
     */
    public static Vector addVectorToVector(Vector vector, Vector toAdd) {
        int numElements = toAdd.size();
        vector.ensureCapacity(vector.size() + toAdd.size());
        for(int i = 0; i < numElements; i++) {
            vector.addElement(toAdd.elementAt(i));
        }
        
        return vector;
    }

    /**
     * If i < 0  - return "0i", else return "i" - E.g. to dislpay 10:01 instead of 10:1
     * @param i Numbr to format
     * @return Number with leading 0 if it's less than 10
     */
    public static String pad0(int i) {
        if(i > 9) {
            return String.valueOf(i);
        }else {
            return "0"+i;
        }
    }

    
    /**
     * This method is here because Arrays.sort is not available in J2ME
     * 
     * @param arr The array to be sorted
     * @param c An interface that implements UMUtil.Comparer
     * 
     * @return The array sorted: the sort is also done to the originally referenced array 
     */
    public static Object[] bubbleSort(Object[] arr, Comparer c) {
        int n = arr.length;
        Object tmp = null;
        
        int j;
        int diff;
        
        boolean swapped = true;
        while(swapped) {
            swapped = false;
            for(j = 1; j < arr.length; j++) {
                diff = c.compare(arr[j-1], arr[j]);
                if(diff > 0) {
                    tmp = arr[j-1];
                    arr[j-1] = arr[j];
                    arr[j] = tmp;
                    swapped = true;
                }
            }
        }
        
        return arr;
    }
    
    /**
     * Copy references from one hashtable to another hashtable
     * 
     * @param src Source hashtable to copy from
     * @param dst Destination hashtable to copy into
     */
    public static void copyHashtable(Hashtable src, Hashtable dst) {
        Enumeration keys = src.keys();
        Object key;
        while(keys.hasMoreElements()) {
            key = keys.nextElement();
            dst.put(key, src.get(key));
        }
    }
    
    public static interface Comparer {
        
        /**
         * Return o1 - 02 as per java.util.Comparator
         * @param o1 First object to be compared
         * @param o2 Second object to be compared
         * 
         * @return 
         */
        public int compare(Object o1, Object o2);
        
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
    public static int indexInArray(Object[] haystack, Object needle, int from, int to) {
        for(int i = from; i < to; i++) {
            if(haystack[i] == null && needle == null) {
                return i;
            }else if(haystack[i] != null && haystack[i].equals(needle)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the index of an item in an array. Filler method because this doesn't existing on J2ME.
     *
     * @param haystack Array to search in
     * @param needle Value to search for
     */
    public static int indexInArray(Object[] haystack, Object needle) {
        return indexInArray(haystack, needle, 0, haystack.length);
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer.
     *
     * @param parser XmlPullParser XML is coming from
     * @param serializer XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     *                                       required e.g. script, style etc. using &lt;/script&gt; instead
     *                                       of &lt;script ... /&gt;
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void passXmlThrough(XmlPullParser parser, XmlSerializer serializer,
                                      String[] seperateEndTagRequiredElements)
            throws XmlPullParserException, IOException{
        PassXmlThroughFilter filter = null;
        passXmlThrough(parser, serializer, seperateEndTagRequiredElements, filter);
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
    public static void passXmlThrough(XmlPullParser parser, XmlSerializer serializer)
            throws XmlPullParserException, IOException{
        PassXmlThroughFilter filter = null;
        passXmlThrough(parser, serializer, null, filter);
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer.
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param separateHtmlEndTagRequiredElements if true then use the default list of html elements
     *                                           that require a separate ending tag e.g. use
     *                                           &lt;/script&gt; instead of &lt;script ... /&gt;
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void passXmlThrough(XmlPullParser xpp, XmlSerializer xs,
                                      boolean separateHtmlEndTagRequiredElements,
                                      PassXmlThroughFilter filter)
            throws XmlPullParserException, IOException{
        passXmlThrough(xpp, xs,
                separateHtmlEndTagRequiredElements ? SEPARATE_END_TAG_REQUIRED_ELEMENTS : null, filter);
    }

    /**
     * Pass XML through from an XmlPullParser to an XmlSerializer. This will not call startDocument
     * or endDocument - that must be called separately. This allows different documents to be merged.
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     *                                       required e.g. script, style etc. using &lt;/script&gt; instead
     *                                       of &lt;script ... /&gt;
     * @param filter XmlPassThroughFilter that can be used to add to output or interrupt processing
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void passXmlThrough(XmlPullParser xpp, XmlSerializer xs,
                                      String[] seperateEndTagRequiredElements,
                                      PassXmlThroughFilter filter)
            throws XmlPullParserException, IOException {

        int evtType = xpp.getEventType();
        int lastEvent = -1;
        String tagName;
        while(evtType != XmlPullParser.END_DOCUMENT) {
            if(filter != null && !filter.beforePassthrough(evtType, xpp, xs))
                return;

            switch(evtType) {
                case XmlPullParser.START_TAG:
                    xs.startTag(xpp.getNamespace(), xpp.getName());
                    for(int i = 0; i < xpp.getAttributeCount(); i++) {
                        xs.attribute(xpp.getAttributeNamespace(i),
                                xpp.getAttributeName(i), xpp.getAttributeValue(i));
                    }
                    break;
                case XmlPullParser.TEXT:
                    xs.text(xpp.getText());
                    break;
                case XmlPullParser.END_TAG:
                    tagName = xpp.getName();

                    if(lastEvent == XmlPullParser.START_TAG
                            && seperateEndTagRequiredElements != null
                            && UMUtil.indexInArray(seperateEndTagRequiredElements, tagName) != -1) {
                        xs.text(" ");
                    }

                    xs.endTag(xpp.getNamespace(), tagName);


                    break;
            }
            if(filter != null && !filter.afterPassthrough(evtType, xpp, xs))
                return;

            lastEvent = evtType;
            evtType = xpp.next();
        }
    }

    /**
     *
     * @param xpp XmlPullParser XML is coming from
     * @param xs XmlSerializer XML is being written to
     * @param seperateEndTagRequiredElements An array of elements where separate closing tags are
     *                                       required e.g. script, style etc. using &lt;/script&gt; instead
     *                                       of &lt;script ... /&gt;
     * @param endTagName If the given endTagName is encountered processing will stop. The endTag
     *                   for this tag will not be sent to the serializer.
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void passXmlThrough(XmlPullParser xpp, XmlSerializer xs,
                                      String[] seperateEndTagRequiredElements,
                                      final String endTagName) throws XmlPullParserException, IOException {
        passXmlThrough(xpp, xs,seperateEndTagRequiredElements, new PassXmlThroughFilter() {
            @Override
            public boolean beforePassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer)
                    throws IOException, XmlPullParserException {
                if(evtType == XmlPullParser.END_TAG && parser.getName() != null
                        && parser.getName().equals(endTagName))
                    return false;
                else
                    return true;
            }

            @Override
            public boolean afterPassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer)
                    throws IOException, XmlPullParserException {
                return true;
            }
        });
    }

    /**
     * Implement this interface to control some of the passXmlThrough methods .  This can be used
     * to add extra output to be serialized or to stop processing.
     */
    public interface PassXmlThroughFilter {

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
        boolean beforePassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer)
                throws IOException, XmlPullParserException;

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
        boolean afterPassthrough(int evtType, XmlPullParser parser, XmlSerializer serializer)
                throws IOException, XmlPullParserException;

    }


    public static String passXmlThroughToString(XmlPullParser xpp, String endTagName) throws XmlPullParserException, IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer xs = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        xs.setOutput(bout, "UTF-8");
        xs.startDocument("UTF-8", Boolean.FALSE);
        passXmlThrough(xpp, xs, null, endTagName);
        xs.endDocument();
        bout.flush();
        String retVal = new String(bout.toByteArray());
        return retVal;
    }

    /**
     * Determine if the two given locales are the same as far as what the user will see.
     *
     * @param oldLocale
     *
     * @return
     */
    public static boolean hasDisplayedLocaleChanged(String oldLocale, Object context) {
        String currentlyDisplayedLocale = UstadMobileSystemImpl.getInstance().getDisplayedLocale(context);
        if(currentlyDisplayedLocale != null && oldLocale != null
                && oldLocale.substring(0, 2).equals(currentlyDisplayedLocale.substring(0,2))) {
            return false;
        }else {
            return true;
        }
    }

    /**
     * Determine if the two given locales are the same language - e.g. en, en-US, en-GB etc.
     *
     * @param locale1
     * @param locale2
     *
     * @return True if the given locales are the same language, false otherwise
     */
    public static boolean isSameLanguage(String locale1, String locale2) {
        if(locale1 == null && locale2 == null) {
            return true;//no language to compare
        }else if(locale1 == null || locale2 == null){
            return false;//one is null
        }else {
            return locale1.substring(0,2).equals(locale2.substring(0, 2));
        }
    }

    /**
     * Generate a new hashtable which is 'flipped' - e.g. where the keys of the input hashtable become
     * the values in the output hashtable, and vice versa.
     *
     * @return Flip hashtable
     */
    public static Hashtable flipHashtable(Hashtable table) {
        Hashtable out = new Hashtable();
        Object key;

        Enumeration keys = table.keys();
        while(keys.hasMoreElements()) {
            key = keys.nextElement();
            out.put(table.get(key), key);
        }

        return out;
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
    public static String joinStrings(Object[] objects, String joiner) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < objects.length; i++) {
            buffer.append(objects.toString());

            if(i < objects.length - 1)
                buffer.append(joiner);
        }

        return buffer.toString();
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
    public static String joinStrings(Vector objects, String joiner) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < objects.size(); i++) {
            buffer.append(objects.elementAt(i));

            if(i < objects.size() - 1)
                buffer.append(joiner);
        }

        return buffer.toString();
    }






}
