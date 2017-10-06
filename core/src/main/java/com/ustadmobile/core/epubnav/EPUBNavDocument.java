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
package com.ustadmobile.core.epubnav;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * This class represents an EPUB navigation document as indicated by the OPF
 * 
 * @author mike
 */
public class EPUBNavDocument {
    
    /**
     * Table of nav tags
     */
    private Hashtable navItems;
    
    public EPUBNavDocument() {
        navItems = new Hashtable();
    }
    
    public static EPUBNavDocument load(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(in);
        return load(xpp);
    }
    
    public static EPUBNavDocument load(XmlPullParser xpp) throws XmlPullParserException, IOException{
        int evtType;
        EPUBNavDocument result = new EPUBNavDocument();
        EPUBNavItem currentNav = null;//represents the current nav tag
        EPUBNavItem currentItem = null;
        int itemDepth = 0;
        String tagName;
        
        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            switch(evtType) {
                case XmlPullParser.START_TAG:
                    tagName = xpp.getName();
                    if(tagName.equals("nav")) {
                        currentNav = new EPUBNavItem(null, null, null, 0);
                        result.navItems.put(xpp.getAttributeValue(null, "id"), 
                            currentNav);
                    }else if(tagName.equals("li")) {
                        currentItem = new EPUBNavItem(currentItem != null ? 
                            currentItem : currentNav, itemDepth);
                        itemDepth++;
                    }else if(tagName.equals("a")) {
                        currentItem.href = xpp.getAttributeValue(null, "href");
                        if(xpp.next() == XmlPullParser.TEXT) {
                            currentItem.title = xpp.getText();
                        }
                    }
                    break;
                    
                case XmlPullParser.END_TAG:
                    if(xpp.getName().equals("nav")) {
                        currentNav = null;
                    }else if(xpp.getName().equals("li")) {
                        currentItem = currentItem.parent;
                        itemDepth--;
                    }
                    
                    break;
            }
        }
        
        
        return result;
    }
    
    public EPUBNavItem getNavById(String id) {
        if(navItems.containsKey(id)) {
            return (EPUBNavItem)navItems.get(id);
        }else {
            return null;
        }
    }
    
}
