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

import com.sun.lwuit.Display;
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
    
    /**
     * Roughly how many bytes get used for each character of text in the LWUIT
     * HTML component.
     * 
     * To repeat this experiment: modify HTMLComponent.java around line 3,000 
     * after:
     * 
     * <pre>
       if (FIXED_WIDTH) {
            comps=showTextFixedWidth(text, curAlign);
       } else {
            comps=showText(text, curAlign);
       }
       </pre>
     * 
     * Memory usage before that statement runs vs. after will tell how many
     * bytes were used.
     *
     */
    public static final int BYTES_PER_CHAR = 230;
    
    
    //TODO: convert to use shared logic now in UMUtil
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
    
    /**
     * If this method returns true it indicates that the current elemeent
     * should not be split into multiple sections.
     * 
     * This will return true for Multichoice idevices to avoid them be split up
     * 
     * @param xpp XmlPullParser which is processing the START_TAG event
     * 
     * @return true if this element is an element not to split across sections, false otherwise (default)
     */
    private static boolean isNoSplitElement(XmlPullParser xpp) {
        String classVal = xpp.getAttributeValue(xpp.getNamespace(), "class");
        if(classVal != null && classVal.indexOf("MultichoiceIdevice") != -1) {
            return true;
        }else {
            return false;
        }
    }
    
    
    /**
     * Divides a page from the given inputstream into an outputstream : starts at 
     * the specified position startLineNum and startColNum and continues until
     * it hits/exceeds the textLenLimit or byteCountLimit
     * 
     * @param in InoutStream source of the page itself
     * @param out OutputStream to which the divided output will be written
     * @param textLenLimit - Maximum number of text characters in tags that are actually displayed to the user
     * @param byteCountLimit - Calculating number of bytes used after which to cut off or -1 for no 
     * @param startlineNum - The line number to start output from
     * @param startColNum - The column number to start output from
     * 
     * @return The line number and column number of the last tag after limits were hit (e.g. use these as parameters for the next section of the page). 
     * 
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public static int[] dividePage(InputStream in, OutputStream out, int textLenLimit, int byteCountLimit, int startlineNum, int startColNum) throws IOException, XmlPullParserException{
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
        int byteCount = 0;
        
        String dontSplitTag = null;
        int dontSplitDepth = -1;
        
        boolean inDontCountSection = false;//used to avoid counting style and script tags
        
        do {
            lineNum = xpp.getLineNumber();
            colNum = xpp.getColumnNumber();
            
            switch(evtType) {
                case XmlPullParser.START_DOCUMENT:
                    xs.startDocument("UTF-8", Boolean.FALSE);
                    break;
                    
                case XmlPullParser.START_TAG:
                    if(xpp.getName().equals("script") || xpp.getName().equals("style")) {
                        inDontCountSection = true;
                    }
                    
                    if(!inRange && (lineNum > startlineNum || lineNum == startlineNum && colNum > startColNum)) {
                        inRange = true;

                        StartTag tag;
                        for(int i = 0; i < openTags.size(); i++) {
                            tag = (StartTag)openTags.elementAt(i);
                            tag.serializeOpening(xs);
                        }
                    }

                    if(inRange && dontSplitTag == null && isNoSplitElement(xpp)) {
                        dontSplitTag = xpp.getName();
                        dontSplitDepth = xpp.getDepth();
                    }

                    if(inRange && xpp.getName().equals("img")) {
                        int width, height;
                        String strVal = xpp.getAttributeValue(null, "width");
                        if(strVal != null) {
                            width = Math.min(Integer.parseInt(strVal), Display.getInstance().getDisplayWidth());
                        }else {
                            width = Display.getInstance().getDisplayWidth();
                        }
                        strVal = xpp.getAttributeValue(null, "height");

                        if(strVal != null) {
                            height = Math.min(Integer.parseInt(strVal), Display.getInstance().getDisplayHeight());
                        }else{
                            height = Display.getInstance().getDisplayHeight();
                        }

                        //Temporary change: causing too many splits
                        //byteCount += width * height * 4;
                    }


                    //TODO: If in range we don't need to keep the attributes 
                    openTags.addElement(new StartTag(xpp));
                    break;
                
                case XmlPullParser.END_TAG:
                    if(xpp.getName().equals("script") || xpp.getName().equals("style")) {
                        inDontCountSection = false;
                    }
                    
                    if(dontSplitTag != null && xpp.getDepth() == dontSplitDepth && xpp.getName().equals(dontSplitTag)) {
                        //that's the end of the don't split section
                        dontSplitTag = null;
                    }

                    if(inRange && (lenCount >= textLenLimit || (byteCount != -1 && byteCount >= byteCountLimit)) && dontSplitTag == null) {
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
                    break;
            }
            
            if(!inDontCountSection && inRange && evtType == XmlPullParser.TEXT) {
                int textLen = xpp.getText().length();
                lenCount += textLen;
                byteCount += textLen * BYTES_PER_CHAR;
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

    }

}
