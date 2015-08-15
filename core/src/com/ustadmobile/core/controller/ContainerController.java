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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.ViewFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Represents a container (e.g. epub file)
 * 
 * 
 * @author mike
 */
public class ContainerController implements UstadController{
    
    private ContainerView containerView;
    
    private String openPath;
    
    private String fileURI; 
    
    private String mimeType;
    
    private UstadJSOPDSEntry entry;
    
    private UstadOCF ocf;
    
    public ContainerController(UstadJSOPDSEntry entry, String openPath, String fileURI, String mimeType) {
        this.entry = entry;
        this.openPath = openPath;
        this.fileURI = fileURI;
        this.mimeType = mimeType;
    }
    
    
    /**
     * 
     * @param entry
     * @param openPath
     * @param fileURI
     * @param mimeType
     * @return 
     */
    public static ContainerController makeFromEntry(UstadJSOPDSEntry entry, String openPath, String fileURI, String mimeType) {
        return new ContainerController(entry, openPath, fileURI, mimeType);
    }
    
    public String getOpenPath() {
        return openPath;
    }
    
    public String getFileURI() {
        return fileURI;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    /**
     * If this is an EPUB container; it can technically container multiple OPF
     * descriptor files
     * 
     * @return 
     */
    public UstadOCF getOCF() throws IOException, XmlPullParserException{
        if(ocf != null) {
            return ocf;
        }
        
        String containerXMLURI = UMFileUtil.joinPaths(
                new String[]{openPath, "META-INF/container.xml"});
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance(); 
        byte[] contentBytes = impl.readURLToString(containerXMLURI, 
            null).getResponse();
                
        XmlPullParser xpp = impl.newPullParser();
        xpp.setInput(new ByteArrayInputStream(contentBytes), "UTF-8");
        ocf = UstadOCF.loadFromXML(xpp);
        return ocf;
    }
    
    public UstadJSOPF getOPF(int index) throws IOException, XmlPullParserException{
        UstadJSOPF opf = null;
        UstadOCF ocf = getOCF();
        String opfPath = UMFileUtil.joinPaths(new String[] {openPath, 
            ocf.rootFiles[index].fullPath});
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance(); 
        XmlPullParser xpp = impl.newPullParser();
        byte[] contentBytes = impl.readURLToString(opfPath, null).getResponse();
        xpp.setInput(new ByteArrayInputStream(contentBytes), "UTF-8");
        opf = UstadJSOPF.loadFromOPF(xpp);
        
        return opf;
    }
    
    
    /**
     * Utility method to return an array of the pages that are in this, if it's an epub
     * @return 
     */
    public UstadJSOPF getEpubPageList() {
        return null;
    }
    
    public void show() {
        if(this.containerView == null) {
            containerView = ViewFactory.makeContainerView(entry, openPath, 
                mimeType);
        }
        containerView.setController(this);
        
        containerView.show();
    }
    
}
