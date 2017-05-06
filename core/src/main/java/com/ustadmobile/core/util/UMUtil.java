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

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
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
     * Request a new port on the DodgyHTTPD Test Server for logging /asset request
     * 
     * @param serverURL - Control Server URL eg http://server:8065/
     * @param action - "newserver" for HTTP server or "newrawserver" for socket logger
     * @param client client name if requesting newrawserver (otherwise null)
     * @return the port that was opened or -1 for an error
     */
    public static int requestDodgyHTTPDPort(String serverURL, String action, String client) {
        try {
            String requestURL = serverURL;
            if(requestURL.indexOf('?') == -1) {
                requestURL += "?action=" + action;
            }else {
                requestURL += "&action=" + action;
            }
            
            if(client != null) {
                requestURL += "&client=" + client;
            }
            
            HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(
                requestURL, new Hashtable(), new Hashtable(), "GET");
            String serverSays = new String(result.getResponse(), "UTF-8");
            JSONObject response = new JSONObject(serverSays);
            int serverPort = response.getInt("port");
            return serverPort;
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.ERROR, 510, action + "," + serverURL, e);
            e.printStackTrace();
            if(e instanceof SecurityException) {
                return PORT_ALLOC_SECURITY_ERR;
            }else if(e instanceof IOException) {
                return PORT_ALLOC_IO_ERR;
            }else {
                return PORT_ALLOC_OTHER_ERR;
            }
        }
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

    public static void passXmlThrough(XmlPullParser xpp, XmlSerializer xs, String endTagName) throws XmlPullParserException, IOException {
        int evtType = xpp.getEventType();
        String tagName;
        while(evtType != XmlPullParser.END_DOCUMENT) {
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
                    if(endTagName != null && endTagName.equals(tagName))
                        return;

                    xs.endTag(xpp.getNamespace(), tagName);


                    break;
            }
            evtType = xpp.next();
        }
    }

    public static String passXmlThroughToString(XmlPullParser xpp, String endTagName) throws XmlPullParserException, IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        XmlSerializer xs = UstadMobileSystemImpl.getInstance().newXMLSerializer();
        xs.setOutput(bout, "UTF-8");
        xs.startDocument("UTF-8", Boolean.FALSE);
        passXmlThrough(xpp, xs, endTagName);
        xs.endDocument();
        bout.flush();
        String retVal = new String(bout.toByteArray());
        return retVal;
    }


    
}
