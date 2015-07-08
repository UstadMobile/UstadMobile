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
package com.ustadmobile.opf;

import com.ustadmobile.opds.UstadJSOPDSItem;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class UstadJSOPF {
    
    private Hashtable mimeExceptions;
    //defaultMimeTypes.put("","");
    
    static Hashtable defaultMimeTypes;
    public static String DEFAULT_MIMETYPE = "application/octet-stream";
    
    public UstadJSOPFItem[] spine;
    
    static {
        setupDefaultMimeTypes();
    }
    
    
    private static void setupDefaultMimeTypes() {
        defaultMimeTypes = new Hashtable();
        defaultMimeTypes.put("gif", "image/gif");
        defaultMimeTypes.put("js", "application/javascript");
        defaultMimeTypes.put("jpg","image/jpg");
        defaultMimeTypes.put("jpeg", "image/jpg");
        defaultMimeTypes.put("png", "image/png");
        defaultMimeTypes.put("svg","image/svg+xml");
        defaultMimeTypes.put("css","text/css");
        defaultMimeTypes.put("html","text/html");
        defaultMimeTypes.put("xml","application/xml");
        defaultMimeTypes.put("xhtml","application/xhtml+xml");
        defaultMimeTypes.put("mp4","video/mp4");
        defaultMimeTypes.put("3gp","video/3gpp");
        defaultMimeTypes.put("avi","video/x-msvideo");
        defaultMimeTypes.put("wmv","video/x-ms-wmv");
        defaultMimeTypes.put("bmp", "image/bmp");
        defaultMimeTypes.put("tiff","image/tiff");
        defaultMimeTypes.put("woff","application/x-font-woff");
        defaultMimeTypes.put("mp3","audio/mpeg");
        defaultMimeTypes.put("wav","audio/wav");
        defaultMimeTypes.put("mid", "audio/midi");
        defaultMimeTypes.put("midi","audio/midi");
        defaultMimeTypes.put("aac","audio/x-aac");
        defaultMimeTypes.put("mj2","video/mj2");
    }
    
    public static final String getExtension(String filename) {
        int dotPos = filename.lastIndexOf('.');
        return dotPos != -1 ? filename.substring(dotPos + 1) : null;
    }
    
    public UstadJSOPF() {
        mimeExceptions = new Hashtable();
    }
    /*
     * xpp: Parser of the OPF
     */
    public static UstadJSOPF loadFromOPF(XmlPullParser xpp) throws XmlPullParserException, IOException {
        UstadJSOPF result = new UstadJSOPF();
        
        
        String extension = null;
        String defMimeType = null;
        int evtType = xpp.getEventType();
        String filename=null;
        String itemMime=null;
        String id=null;
        String properties=null;
        String idref=null;
        Hashtable allItems = new Hashtable();
        Vector spineItems = new Vector();        

        do
        {
            filename=null;
            itemMime=null;
            id=null;
            properties=null;
            defMimeType = null;
            extension=null;
            idref=null;
            
            if(evtType == XmlPullParser.START_TAG){
                if(xpp.getName().equals("manifest")){
                    System.out.println("In Manifest: " + xpp.getName());
                }else if(xpp.getName() != null && xpp.getName().equals("item")){
                    
                    filename=xpp.getAttributeValue(null, "href");
                    System.out.println("item: " + filename);
                    itemMime=xpp.getAttributeValue(null, "media-type");
                    id = xpp.getAttributeValue(null, "id");
                    properties = xpp.getAttributeValue(null, "properties");

                    extension=getExtension(filename);
                    if(extension != null && defaultMimeTypes.containsKey(extension)){
                        defMimeType = (String)defaultMimeTypes.get(extension);
                    }
                    if(extension == null || defMimeType == null ||
                            !itemMime.equals(defMimeType)){
                        result.mimeExceptions.put(filename, itemMime);
                    }
                    UstadJSOPFItem item2 = new UstadJSOPFItem();
                    item2.href = filename;
                    item2.mimeType = itemMime;
                    item2.properties = properties;                        

                    allItems.put(id, item2);

                }else if(xpp.getName() != null && xpp.getName().equals("itemref")){
                    //for each itemRef in spine
                    //if(xpp.getName().equals("itemref")){
                    idref=xpp.getAttributeValue(null, "idref");
                    Object spineItem = allItems.get(idref);
                    if(spineItem == null){
                        throw new RuntimeException("Invalid OPF: item not found: #" + idref);
                    }
                        
                    spineItems.addElement(allItems.get(idref));
                    
                }
                    
                
            }else if(evtType == XmlPullParser.END_TAG){
                if(xpp.getName().equals("manifest")){
                    System.out.println("End of manifest.");
                }else if(xpp.getName().equals("spine")){
                    result.spine = new UstadJSOPFItem[spineItems.size()];
                    spineItems.copyInto(result.spine);
                    
                }
            }else if(evtType == XmlPullParser.TEXT){

            }
            evtType = xpp.next();
            
        }while(evtType != XmlPullParser.END_DOCUMENT);
        
        return result;
    }
    
    public String getMimeType(String filename) {
        if(mimeExceptions.containsKey(filename)) {
            return (String)mimeExceptions.get(filename);
        }
        
        String extension = filename.substring(filename.lastIndexOf('.'));
        if(defaultMimeTypes.containsKey(extension)) {
            return (String)defaultMimeTypes.get(extension);
        }
        
        return DEFAULT_MIMETYPE;
    }
    
}
