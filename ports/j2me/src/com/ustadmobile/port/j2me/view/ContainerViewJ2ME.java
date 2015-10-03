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
import com.sun.lwuit.Display;
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
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author mike
 */
public class ContainerViewJ2ME extends UstadViewFormJ2ME implements ContainerView, ActionListener, ControllerReadyListener{
        
    private ContainerController controller;
    
    private String title;
    
    private int currentIndex;
    
    private UstadOCF ocf;
    
    private UstadJSOPF opf;
    
    private DocumentRequestHandler requestHandler;
    
    private String containerURI;
    
    private String mimeType;
    
    private ZipFileHandle containerZip;
    
    private String openContainerBaseURI;
    
    private HTMLComponent htmlC;
    
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
    
    static {
        mediaExtensions = new Hashtable();
        mediaExtensions.put("mp3", "audio/mpeg");
    }
    
    
    public ContainerViewJ2ME(Hashtable args, Object context) {
        super(args, context);
        UstadMobileSystemImplJ2ME impl = UstadMobileSystemImplJ2ME.getInstanceJ2ME();
        containerURI = (String)args.get(ContainerController.ARG_CONTAINERURI);
        mimeType = (String)args.get(ContainerController.ARG_MIMETYPE);
        
        cmdBack = new Command(impl.getString(U.id.back), CMDBACK_ID);
        cmdForward = new Command(impl.getString(U.id.next), CMDFORWARD_ID);
        
        openContainerBaseURI = impl.openContainer(containerURI, mimeType);
        containerZip = impl.getOpenZip();
        setLayout(new BorderLayout());
        
        UstadMobileSystemImpl.l(UMLog.ERROR, 175, containerURI);
        
        //TODO: localize this string
        impl.getAppView(context).showAlertDialog(impl.getString(U.id.error), 
            "Could not open container");
        ContainerController.makeControllerForView(this, openContainerBaseURI, 
                mimeType, this);
        
        requestHandler = new ContainerDocumentRequestHandler(this);
        htmlCallback = new ContainerHTMLCallback(this);
    }

    public void controllerReady(UstadController controller, int flags) {
        if(controller != null) {
            setController((ContainerController)controller);
            initByContentType();
        }
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
        super.show();
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
    }

    public boolean isShowing() {
        return isVisible();
    }
    
    public void initByContentType() {
        if(controller.getMimeType().startsWith("application/epub+zip")) {
            initEPUB();
        }
    }
    
    protected void initEPUB() {
        try {
            HTTPUtils.httpDebug("getting ocf");
            if (controller == null){
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 524, "");
            }else{
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.DEBUG, 526, "");
            }
            UstadOCF ocf = controller.getOCF();
            HTTPUtils.httpDebug("getting opf");
            UstadJSOPF opf = controller.getOPF(0);
            HTTPUtils.httpDebug("getting spine");
            spineURLs = opf.getLinearSpineURLS();
            HTTPUtils.httpDebug("getting requesthandler");
            
            HTTPUtils.httpDebug("getting htmlcallback");
            
            htmlC = new HTMLComponent(requestHandler);
            htmlC.setHTMLCallback(htmlCallback);
            htmlC.setImageConstrainPolicy(
                HTMLComponent.IMG_CONSTRAIN_WIDTH | HTMLComponent.IMG_CONSTRAIN_HEIGHT);
            htmlC.setIgnoreCSS(true);
            addCommand(cmdBack);
            addCommand(cmdForward);
            addCommandListener(this);
            
            HTTPUtils.httpDebug("getting opfurl");
            opfURL = UMFileUtil.joinPaths(
                    new String[]{UstadMobileSystemImplJ2ME.OPENZIP_PROTO, 
                    ocf.rootFiles[0].fullPath});
            HTTPUtils.httpDebug("opfURL:" + opfURL);
            HTTPUtils.httpDebug("title: " + title);
            Display.getInstance().callSerially(new Runnable() {
                public void run() {
                    addComponent(BorderLayout.CENTER, htmlC);
                    showPage(1);
                }
            });
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 350, null, e);
        }
    }
    
    public void showPage(int pageIndex) {
        if(pageIndex == currentIndex) {
            return;
        }
                
        htmlC.setPage(UMFileUtil.resolveLink(opfURL, spineURLs[pageIndex]));
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

    public void setContainerTitle(String containerTitle) {
        setTitle(containerTitle);
    }
    
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
            boolean isComplete = status == STATUS_COMPLETED;
            boolean isDisplayed = status == STATUS_DISPLAYED;
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
