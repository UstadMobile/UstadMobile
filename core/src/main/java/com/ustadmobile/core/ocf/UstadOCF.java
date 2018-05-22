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

package com.ustadmobile.core.ocf;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Vector;


/**
 * This class exists to quickly parse container.xml files that give a list
 * of root elements in the container (e.g. zip) file
 * 
 * @author mike
 */
public class UstadOCF {
    
    public UstadOCF(UstadOCFRootFile[] rootFiles) {
        this.rootFiles = rootFiles;
    }

    public UstadOCF() {

    }
    
    public UstadOCFRootFile[] rootFiles;
    
    public static final String ROOTFILETAG = "rootfile";
    
    public static UstadOCF loadFromXML(XmlPullParser xpp) throws XmlPullParserException, IOException{
        int evtType = 0;
        
        Vector rootsFound = new Vector();
        do {
            evtType = xpp.next();
            if(evtType == XmlPullParser.START_TAG) {
                if(ROOTFILETAG.equals(xpp.getName())) {
                    String fullPath = xpp.getAttributeValue(null, "full-path");
                    String mediaType = xpp.getAttributeValue(null, "media-type");
                    rootsFound.addElement(new UstadOCFRootFile(fullPath, 
                        mediaType));
                }
            }
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        UstadOCFRootFile[] rootFiles = new UstadOCFRootFile[rootsFound.size()];
        rootsFound.copyInto(rootFiles);
        
        UstadOCF retVal = new UstadOCF(rootFiles);
        return retVal;
    }


    public void loadFromParser(XmlPullParser xpp) throws XmlPullParserException, IOException{
        int evtType = 0;

        Vector rootsFound = new Vector();
        do {
            evtType = xpp.next();
            if(evtType == XmlPullParser.START_TAG) {
                if(ROOTFILETAG.equals(xpp.getName())) {
                    String fullPath = xpp.getAttributeValue(null, "full-path");
                    String mediaType = xpp.getAttributeValue(null, "media-type");
                    rootsFound.addElement(new UstadOCFRootFile(fullPath,
                            mediaType));
                }
            }
        }while(evtType != XmlPullParser.END_DOCUMENT);

        rootFiles = new UstadOCFRootFile[rootsFound.size()];
        rootsFound.copyInto(rootFiles);
    }
}


