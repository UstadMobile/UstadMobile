/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.app;

import com.ustadmobile.port.j2me.app.controller.UstadMobileAppController;
import com.ustadmobile.core.opf.UstadJSOPF;
import gnu.classpath.java.util.zip.ZipInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class EpubUtils {
    
    
    public static String getOpfPathFromEpub(String epubFile) 
            throws IOException, XmlPullParserException{
        ZipInputStream stream = ZipUtils.readFileFromZip(epubFile, 
                FileUtils.EPUB_CONTAINER);

        KXmlParser parser = new KXmlParser();
        parser = (KXmlParser) UstadMobileAppController.parseXml(stream);
        
        //Extract file contents
        String epubPackageFile = "";
        int evtType = parser.getEventType();
        do{
            evtType = parser.next();   
            String name = parser.getName();
            if (name != null && name.equals("rootfile")){
                epubPackageFile = parser.getAttributeValue(null, "full-path");
                break;
            }
            
        }while(evtType != XmlPullParser.END_DOCUMENT);
        System.out.println(epubPackageFile);
        return epubPackageFile;
    }
    
    public static UstadJSOPF getOpfFromEpub(String epubFile) 
            throws IOException, XmlPullParserException{
        //List
        String epubPackageFile = getOpfPathFromEpub(epubFile);
        ZipInputStream opfStream = ZipUtils.readFileFromZip(epubFile, epubPackageFile);
        KXmlParser opfParser = new KXmlParser();
        opfParser = (KXmlParser) UstadMobileAppController.parseXml(opfStream);
        
        UstadJSOPF opfObj = UstadJSOPF.loadFromOPF(opfParser);
        
        return opfObj;
    }
    
    public static String getOpfLocationFromEpub(String epubFile) 
            throws IOException, XmlPullParserException{
        return FileUtils.getFolderPath(getOpfPathFromEpub(epubFile));
    }
    
    public static InputStream getPageFromEpub(String epubFile, String pageName) 
            throws IOException{
        
        return ZipUtils.readFileFromZip(epubFile, pageName);
    }
    
    
}
