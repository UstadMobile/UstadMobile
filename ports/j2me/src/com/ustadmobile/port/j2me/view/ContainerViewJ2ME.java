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
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.layouts.BorderLayout;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
    
    
    public ContainerViewJ2ME(UstadJSOPDSEntry entry, String openPath, String mime) {
        containerZip = UstadMobileSystemImplJ2ME.getInstanceJ2ME().getOpenZip();
        cmdBack = new Command("Back", CMDBACK_ID);
        cmdForward = new Command("Next", CMDFORWARD_ID);
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
            UstadOCF ocf = controller.getOCF();
            UstadJSOPF opf = controller.getOPF(0);
            spineURLs = opf.getLinearSpineURLS();
            requestHandler = new ContainerDocumentRequestHandler(this);
            opfURL = UMFileUtil.joinPaths(
                    new String[]{UstadMobileSystemImplJ2ME.OPENZIP_PROTO, 
                    ocf.rootFiles[0].fullPath});
            showPage(1);
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 350, null, e);
        }
    }
    
    public void showPage(int pageIndex) {
        if(pageIndex == currentIndex) {
            return;
        }
        
        Form oldForm = currentForm;
        currentForm = new Form();
        currentForm.setLayout(new BorderLayout());
        HTMLComponent comp = new HTMLComponent(requestHandler);
        comp.setImageConstrainPolicy(
            HTMLComponent.IMG_CONSTRAIN_WIDTH | HTMLComponent.IMG_CONSTRAIN_HEIGHT);
        comp.setIgnoreCSS(true);
        comp.setPage(UMFileUtil.resolveLink(opfURL, spineURLs[pageIndex]));
        currentForm.addComponent(BorderLayout.CENTER, comp);
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
    
}
