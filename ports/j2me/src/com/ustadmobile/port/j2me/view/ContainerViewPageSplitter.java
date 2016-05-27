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
package com.ustadmobile.port.j2me.view;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * On J2ME resources are limited and left unchecked large pages will crash
 * the LWUIT browser.  
 * 
 * @author mike
 */
public class ContainerViewPageSplitter {
    
    /**
     * The parameter name in the URL that indicates the current index
     */
    public static final String SPLIT_INDEX_ARG = "s-index";
    
    public static final int POS_FINAL_SECTION = -100;
    
    public static void passThrough(XmlPullParser xpp, XmlSerializer xs) throws IOException, XmlPullParserException{
        int evtType = xpp.getEventType();
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
                xs.endTag(xpp.getNamespace(), xpp.getName());
                break;
        }
    }
    
    public static String makeSplitLink(String baseURL, Hashtable urlParams, int sectionIndex, int dir, int endLineNum, int endColNum) {
        Hashtable newParams = new Hashtable(dir == 1 ? 
            urlParams.size() + 1 : urlParams.size());
        
        UMUtil.copyHashtable(urlParams, newParams);
        String pageIndexKey = "page-" + sectionIndex;
        if(dir == 1) {
            //next page section link
            newParams.put(pageIndexKey, "" + endLineNum + '-' + endColNum);
        }else {
            //previous page section link
            newParams.remove(pageIndexKey);
        }
        
        return baseURL + '?' + UMFileUtil.hashtableToQueryString(newParams);
    }
    
    public static int[] dividePage(InputStream in, OutputStream out, int textLenLimit, int startlineNum, int startColNum) throws IOException, XmlPullParserException{
        KXmlParser xpp = new KXmlParser();
        XmlSerializer xs = new KXmlSerializer();
        xs.setOutput(out, "UTF-8");
        xpp.setInput(in, "UTF-8");
        boolean inRange = false;
        
        int evtType = xpp.getEventType();
        Vector openTags = new Vector();
        
        int lineNum;
        int colNum;
        int lenCount = 0;
        
        do {
            lineNum = xpp.getLineNumber();
            colNum = xpp.getColumnNumber();
            
            if(evtType == XmlPullParser.START_DOCUMENT) {
                xs.startDocument("UTF-8", Boolean.FALSE);
            }else if(evtType == XmlPullParser.START_TAG) {
                if(!inRange && (lineNum > startlineNum || lineNum == startlineNum && colNum >= startColNum)) {
                    inRange = true;
                    
                    StartTag tag;
                    for(int i = 0; i < openTags.size(); i++) {
                        tag = (StartTag)openTags.elementAt(i);
                        tag.serializeOpening(xs);
                    }
                }
                
                //TODO: If in range we don't need to keep the attributes 
                openTags.addElement(new StartTag(xpp));                
            }else if(evtType == XmlPullParser.END_TAG) {
                if(inRange && lenCount >= textLenLimit) {
                    StartTag tag;
                    for(int i = openTags.size()-1; i >= 0; i--) {
                        //close tags that need closed
                        tag = (StartTag)openTags.elementAt(i);
                        tag.serializeEnd(xs);
                    }
                    
                    xs.endDocument();
                    return new int[] {lineNum, colNum};
                }
                
                openTags.removeElementAt(openTags.size()-1);
            }
            
            if(inRange && evtType == XmlPullParser.TEXT) {
                lenCount += xpp.getText().length();
            }
            
            if(inRange) {
                 passThrough(xpp, xs);
            }
            
            evtType = xpp.next();
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        return new int[]{POS_FINAL_SECTION, POS_FINAL_SECTION};
    }
    
    
    public static class StartTag {

        private Hashtable attrs;

        private String tagName;

        private String namespace;

        public StartTag(XmlPullParser xpp) {
            tagName = xpp.getName();
            namespace = xpp.getNamespace();

            if(xpp.getAttributeCount() > 0) {
                attrs = new Hashtable();
                final int numAttrs = xpp.getAttributeCount();
                for(int i = 0; i < numAttrs; i++) {
                    attrs.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                }
            }
        }

        public void serializeOpening(XmlSerializer xs) throws IOException{
            xs.startTag(namespace, tagName);
            if(attrs != null) {
                Enumeration e = attrs.keys();
                String aName;
                while(e.hasMoreElements()) {
                    aName = (String)e.nextElement();
                    xs.attribute(null, aName, (String)attrs.get(aName));
                }
            }
        }

        public void serializeEnd(XmlSerializer xs) throws IOException {
            xs.endTag(namespace, tagName);
        }

        /**
         * Returns the opening of the tag (only)
         * @return 
         */
        public String getOpening() {
            StringBuffer retVal = new StringBuffer();
            retVal.append('<').append(tagName);

            if(attrs != null) {
                Enumeration e = attrs.keys();
                String attrName;
                while(e.hasMoreElements()) {
                    attrName = (String)e.nextElement();
                    retVal.append(' ');
                    retVal.append(attrName).append('=').append('\"');
                    retVal.append((String)attrs.get(attrName));
                    retVal.append('\"');
                }
            }

            retVal.append('>');
            return retVal.toString();
        }

        public String getClosing() {
            return "</" + tagName +">";
        }

    }

}
