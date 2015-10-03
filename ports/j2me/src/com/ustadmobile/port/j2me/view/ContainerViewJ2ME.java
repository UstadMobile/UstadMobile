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

import com.sun.lwuit.Command;
import com.sun.lwuit.Form;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.DefaultHTMLCallback;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.sun.lwuit.layouts.BorderLayout;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.util.UMTinCanUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import org.json.me.JSONObject;

/**
 *
 * @author mike
 */
public class ContainerViewJ2ME implements ContainerView, ActionListener{
    
    private Form currentForm;
    
    private ContainerController controller;
    
    private String title;
    
    private int currentIndex;
    
    private UstadOCF ocf;
    
    private UstadJSOPF opf;
    
    private DocumentRequestHandler requestHandler;
    
    private ZipFileHandle containerZip;
    
    /**
     * The OPF base URL within the zip of the container epub that is being shown
     */
    private String opfURL;
    
    private String[] spineURLs;
    
    private Command cmdBack;
    
    private Command cmdForward;
    
    public static final int CMDBACK_ID = 1;
    
    public static final int CMDFORWARD_ID = 2;
    
    private HTMLCallback htmlCallback;
    
    static Hashtable mediaExtensions;
    
    long lastPageChangeTime = -1;
    
    /**
     * The HTML component with the current page
     */
    private HTMLComponent htmlC;
    
    static {
        mediaExtensions = new Hashtable();
        mediaExtensions.put("mp3", "audio/mpeg");
    }
    
    
    public ContainerViewJ2ME() {
        UstadMobileSystemImplJ2ME impl = UstadMobileSystemImplJ2ME.getInstanceJ2ME();
        containerZip = impl.getOpenZip();
        cmdBack = new Command(impl.getString(U.id.back), CMDBACK_ID);
        cmdForward = new Command(impl.getString(U.id.next), CMDFORWARD_ID);
    }

    public void setController(ContainerController controller) {
        this.controller = controller;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void show() {
        initByContentType();
    }

    public boolean isShowing() {
        return currentForm != null && currentForm.isVisible();
    }
    
    public void initByContentType() {
        if(controller.getMimeType().startsWith("application/epub+zip")) {
            initEPUB();
        }
    }
    
    protected void initEPUB() {
        try {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 597 , null);
            if (controller == null){
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 524, "");
            }else{
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 526, "");
            }
            
            ocf = controller.getOCF();
            opf = controller.getOPF(0);
            spineURLs = opf.getLinearSpineURLS();
            requestHandler = new ContainerDocumentRequestHandler(this);
            htmlCallback = new ContainerHTMLCallback(this);
            opfURL = UMFileUtil.joinPaths(
                    new String[]{UstadMobileSystemImplJ2ME.OPENZIP_PROTO, 
                    ocf.rootFiles[0].fullPath});
            showPage(1);
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 350, null, e);
        }
    }
    
    /**
     * Make a tincan statement about the user viewing the current page and
     * send it to the system implementation to be queued.
     */
    protected void makePageStatement() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(lastPageChangeTime != -1 && impl.getActiveUser() != null) {
            long duration =  System.currentTimeMillis() - lastPageChangeTime;
            JSONObject actor = UMTinCanUtil.makeActorFromUserAccount(
                impl.getActiveUser(), 
                UstadMobileSystemImpl.getInstance().getAppPref(
                    UstadMobileSystemImpl.PREFKEY_XAPISERVER,
                    UstadMobileDefaults.DEFAULT_XAPI_SERVER));
            String currentPage = spineURLs[currentIndex];
            
            //TODO: get language of the page itself or from OPF
            String title = currentPage;
            if(htmlC != null && htmlC.getTitle() != null) {
                title = htmlC.getTitle();
            }
            
            JSONObject stmt = UMTinCanUtil.makePageViewStmt(opf.id, currentPage,
                title, "en-US", duration, actor);
            UstadMobileSystemImpl.getInstance().queueTinCanStatement(stmt);
        }
    }
    
    /**
     * Show the page as per the index in the spineURLs
     * 
     * @param pageIndex Index of page number to show
     */
    public void showPage(int pageIndex) {
        if(pageIndex == currentIndex) {
            return;
        }
        
        makePageStatement();
        
        lastPageChangeTime = System.currentTimeMillis();
        
        Form oldForm = currentForm;
        currentForm = new Form();
        currentForm.setLayout(new BorderLayout());
        htmlC = new HTMLComponent(requestHandler);
        htmlC.setHTMLCallback(htmlCallback);
        htmlC.setImageConstrainPolicy(
            HTMLComponent.IMG_CONSTRAIN_WIDTH | HTMLComponent.IMG_CONSTRAIN_HEIGHT);
        htmlC.setIgnoreCSS(true);
        htmlC.setPage(UMFileUtil.resolveLink(opfURL, spineURLs[pageIndex]));
        currentForm.addComponent(BorderLayout.CENTER, htmlC);
        currentForm.show();
        
        currentForm.addCommand(cmdBack);
        currentForm.addCommand(cmdForward);
        currentForm.addCommandListener(this);
        this.currentIndex = pageIndex;
    }

    public void actionPerformed(ActionEvent ae) {
        Command cmd = ae.getCommand();
        if(cmd.equals(cmdBack)) {
            showPage(this.currentIndex -1);
        }else if(cmd.equals(cmdForward)) {
            showPage(this.currentIndex + 1);
        }
    }
    
    
    /**
     * Handles requests to load resources by pointing them to the openzip and
     * requests to play media
     */
    public class ContainerDocumentRequestHandler implements DocumentRequestHandler {

        private ContainerViewJ2ME view;
        
        
        public ContainerDocumentRequestHandler(ContainerViewJ2ME view) {
            this.view = view;
        }
        
        public InputStream resourceRequested(DocumentInfo di) {
            try {
                String requestURL = di.getUrl();
                System.out.println("requestURL " + requestURL);
                String baseURL = di.getBaseURL();
                System.out.println("baseURL " + baseURL);
                String pathInZip = di.getUrl().substring(
                    UstadMobileSystemImplJ2ME.OPENZIP_PROTO.length());
                return view.containerZip.openInputStream(pathInZip);
            }catch(IOException e) {
                return new ByteArrayInputStream("ERROR".getBytes());
            }
        }
        
    }
    
    public class ContainerHTMLCallback extends DefaultHTMLCallback {

        private ContainerViewJ2ME view;
        
        private Timer timer = null;
        
        public ContainerHTMLCallback(ContainerViewJ2ME view) {
            this.view = view;
            
        }

        public void pageStatusChanged(HTMLComponent htmlC, int status, String url) {
            super.pageStatusChanged(htmlC, status, url); //To change body of generated methods, choose Tools | Templates.
        }
        
        public void mediaPlayRequested(final int type, final int op, final HTMLComponent htmlC, final String src, final HTMLElement mediaElement) {
            if(timer == null) {
                timer = new Timer();
            }
            
            timer.schedule(new TimerTask() {
                public void run() {
                    boolean isPlaying = false;
                    InputStream in= null;
                    String source = mediaElement.getAttributeById(HTMLElement.ATTR_SRC);
                    if(source == null) {
                        //means the source is not on the tag itself but on the source tags - find them
                        HTMLElement srcTag = mediaElement.getFirstChildByTagId(
                                HTMLElement.TAG_SOURCE);
                        if(srcTag != null) {
                            source = srcTag.getAttributeById(HTMLElement.ATTR_SRC);
                        }
                    }
                    
                    if(source != null) {
                        try {
                            String fullURI = UMFileUtil.resolveLink(
                                htmlC.getPageURL(), source);
                            String pathInZip = fullURI.substring(
                                UstadMobileSystemImplJ2ME.OPENZIP_PROTO.length());
                            String mediaFileExtension = UMFileUtil.getExtension(src);
                            Object mediaTypeObj = mediaExtensions.get(mediaFileExtension);

                            UstadMobileSystemImpl.l(UMLog.VERBOSE, 427, pathInZip 
                                + ':' + mediaFileExtension + ':' + mediaTypeObj);

                            in = view.containerZip.openInputStream(pathInZip);

                            if(mediaTypeObj != null) {
                                isPlaying = UstadMobileSystemImplJ2ME.getInstanceJ2ME().playMedia(in, 
                                    (String)mediaTypeObj);
                            }else {
                                UstadMobileSystemImpl.l(UMLog.INFO, 120, src);
                            }

                        }catch(IOException e) {
                            UstadMobileSystemImpl.l(UMLog.ERROR, 120, src, e);
                        }finally {
                            if(!isPlaying) {
                                UMIOUtils.closeInputStream(in);
                            }
                        }
                    }else {
                        UstadMobileSystemImpl.l(UMLog.INFO, 373, mediaElement.toString());
                    }
                    
                }
                
            }, 1000);
        }
        
    }
    
}
