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
import com.ustadmobile.core.impl.ZipEntryHandle;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.UstadView;
import java.io.InputStream;
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
    
    public static final String PREFKEY_PREFIX_LASTOPENED = "laxs-";
        
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
     * Hardcoded fixed path to the container.xml file as per the open container
     * format spec : META-INF/container.xml
     */
    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";
    
    /**
     * Harded postfix added to container files when downloading the thumbnail
     * for them.  E.g. For a book called bookname.epub where the PNG thumbnail
     * was given in the OPDS feed we will have a file called bookname.epub.thumb.png
     */
    public static final String THUMBNAIL_POSTFIX = ".thumb.";
    
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
    
    /**
     * Generates an OPDS feed for the entries in a given container.  In the case
     * of an epub this is all the 
     * 
     * @param fileURI File URI to the path of an EPUB file
     * @param cachePath The path in which the cached container feed is to be written
     * 
     * @return UstadJSOPDSFeed when 
     * 
     * @throws IOException 
     */
    public static UstadJSOPDSFeed generateContainerFeed(String fileURI, String cachePath) throws IOException{
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.l(UMLog.VERBOSE, 437, fileURI);
        
        String containerFilename = UMFileUtil.getFilename(fileURI);
        String cacheFeedID = CatalogController.sanitizeIDForFilename(fileURI);
        UstadJSOPDSFeed result = new UstadJSOPDSFeed(fileURI, containerFilename, 
            cacheFeedID);
        
        String absFileURI = UMFileUtil.ensurePathHasPrefix("file://", fileURI);
        
        //check and see if there is a given default thumbnail for this container
        String[] imgExtensions = new String[]{"jpg", "png", "gif"};
        String thumbURI = null;
        String thumbMimeType = null;
        
        for(int i = 0; i < imgExtensions.length; i++) {
            try {
                thumbURI = absFileURI + THUMBNAIL_POSTFIX + imgExtensions[i];
                if(impl.fileExists(thumbURI)) {
                    thumbMimeType = impl.getMimeTypeFromExtension(imgExtensions[i]);
                    break;
                }
            }catch(Exception e) {
                impl.l(UMLog.ERROR, 150, thumbURI, e);
            }
        }
        
        
        ZipFileHandle zipHandle = null;
        InputStream zIs = null;
        UstadOCF ocf;
        UstadJSOPF opf;
        UstadJSOPDSEntry epubEntry;
        int j;
        
        try {
            zipHandle = impl.openZip(fileURI);
            zIs = zipHandle.openInputStream(OCF_CONTAINER_PATH);
            ocf = UstadOCF.loadFromXML(impl.newPullParser(zIs));
            UMIOUtils.closeInputStream(zIs);
            
            for(j = 0; j < ocf.rootFiles.length; j++) {
                zIs = zipHandle.openInputStream(ocf.rootFiles[j].fullPath);
                opf = UstadJSOPF.loadFromOPF(impl.newPullParser(zIs), 
                        UstadJSOPF.PARSE_METADATA);
                UMIOUtils.closeInputStream(zIs);
                zIs = null;
                    
                epubEntry =new UstadJSOPDSEntry(result,opf, 
                    UstadJSOPDSItem.TYPE_EPUBCONTAINER, absFileURI);
                if(thumbMimeType != null) {//Thumb Mime type only set when we have a file
                    epubEntry.addLink(UstadJSOPDSEntry.LINK_THUMBNAIL, 
                        thumbMimeType, thumbURI);
                }
                
                result.addEntry(epubEntry);
            }
        }catch(Exception e) {
            impl.l(UMLog.ERROR, 142, fileURI, e);
        }finally {
            UMIOUtils.closeInputStream(zIs);
            UMIOUtils.closeZipFileHandle(zipHandle);
        }
        
        
        return result;
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
     * Log that the given container has been opened.  This should be called
     * by the view.  it can then be used as the basis by which to sort items
     * 
     * @param opf 
     */
    public void logContainerOpened(UstadJSOPF opf) {
        UstadMobileSystemImpl.getInstance().setUserPref(
            PREFKEY_PREFIX_LASTOPENED + opf.id, ""+System.currentTimeMillis(), 
            context);
    }
    
    /**
     * Get the time (in miliseconds since 1/1/1970 as per system.currenTimeMillis)
     * 
     * @param id Container ID to find the last time opened
     * @param context Context object for retrieving preferences
     * 
     * @return 
     */
    public static long getContainerLastOpenedTime(String id, Object context) {
        return Long.parseLong(UstadMobileSystemImpl.getInstance().getUserPref(
            PREFKEY_PREFIX_LASTOPENED + id, "0", context));
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
