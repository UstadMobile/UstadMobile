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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.view.UstadView;
import java.util.Hashtable;

/**
 * Represents a container (e.g. epub file)
 * 
 * 
 * @author mike
 */
public class ContainerController extends UstadBaseController implements AsyncLoadableController{
    
    private ContainerView containerView;
    
    private String openPath;
        
    private String mimeType;
    
    private UstadJSOPDSEntry entry;
    
    private UstadOCF ocf;
    
    private String[] opfTitles;
        
    /**
     * Use with loadController as the key for the containerURI in args hashtable
     * @see ContainerController#loadController(java.util.Hashtable) 
     */
    public static final String ARG_CONTAINERURI = "URI";
    
    /**
     * Use with loadController as the key for the mime type in args hashtable
     * @see ContainerController#loadController(java.util.Hashtable) 
     */
    public static final String ARG_MIMETYPE = "MIME";
    
    /**
     * Empty constructor - this creates a blank unusable object - required for async loading
     */
    public ContainerController(Object context) {
         super(context);
    }
    
    public static void makeControllerForView(ContainerView view, String containerURI, String mimeType, ControllerReadyListener listener) {
        Hashtable args = new Hashtable();
        args.put(ARG_CONTAINERURI, containerURI);
        args.put(ARG_MIMETYPE, mimeType);
        ContainerController ctrl = new ContainerController(view.getContext());
        new LoadControllerThread(args, ctrl, listener, view).start();
    }
    
    public String getOpenPath() {
        return openPath;
    }
    
    
    public String getMimeType() {
        return mimeType;
    }
    
    public UstadView getView() {
        return containerView;
    }
    
    public void setView(UstadView view) {
        this.containerView = (ContainerView)view;
    }
    
    /**
     * If this is an EPUB container; it can technically container multiple OPF
     * descriptor files
     * 
     * @return 
     */
    public UstadOCF getOCF() throws IOException, XmlPullParserException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        
        if(ocf != null) {
            impl.getLogger().l(UMLog.DEBUG, 528, "");
            return ocf;
        }
        impl.getLogger().l(UMLog.DEBUG, 530, "");
        String containerXMLURI = UMFileUtil.joinPaths(
                new String[]{openPath, "META-INF/container.xml"});
        
        impl.getLogger().l(UMLog.DEBUG, 522, containerXMLURI);
        HTTPResult res = UstadMobileSystemImpl.getInstance().readURLToString(containerXMLURI, 
            null);
        impl.getLogger().l(UMLog.DEBUG, 534, "result got");
        byte[] contentBytes2 = res.getResponse();
        String b = new String(contentBytes2);
        impl.getLogger().l(UMLog.DEBUG, 534, "b is :" + b);
        byte[] contentBytes = impl.readURLToString(containerXMLURI, 
            null).getResponse();
        String a = new String(contentBytes);
        impl.getLogger().l(UMLog.DEBUG, 534, a);
        
        impl.getLogger().l(UMLog.DEBUG, 534, "Starting to get ocf");
        XmlPullParser xpp = impl.newPullParser();
        xpp.setInput(new ByteArrayInputStream(contentBytes), "UTF-8");
        ocf = UstadOCF.loadFromXML(xpp);
        impl.getLogger().l(UMLog.DEBUG, 534, "Got ocf");
        
        opfTitles = new String[ocf.rootFiles.length];
        impl.getLogger().l(UMLog.DEBUG, 534, null);
        
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
    
    /**
     * Load this controller - used by the async thread basesd loader
     * 
     * @param args should include the containeruri and mimetype as per ARG constants
     * @return
     * @throws Exception 
     * @see ContainerController#ARG_CONTAINERURI
     * @see ContainerController#ARG_MIMETYPE
     */
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        openPath = (String)args.get(ARG_CONTAINERURI);
        mimeType = (String)args.get(ARG_MIMETYPE);
        getOCF();
        return this;
    }

    public void setUIStrings() {
        //do nothing - there are no ui strings to be set.
    }
    
}
