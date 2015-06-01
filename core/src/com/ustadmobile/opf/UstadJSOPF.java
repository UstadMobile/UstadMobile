/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.app.opf;

import com.ustadmobile.app.UstadJS;
import com.ustadmobile.app.opds.UstadJSOPDSItem;
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

                    extension=UstadJS.getExtension(filename);
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
