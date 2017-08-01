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
import com.sun.lwuit.List;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.html.AsyncDocumentRequestHandler;
import com.sun.lwuit.html.AsyncDocumentRequestHandler.IOCallback;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.sun.lwuit.layouts.BorderLayout;
import com.sun.lwuit.mediaplayer.DefaultLWUITMediaPlayerManager;
import com.sun.lwuit.mediaplayer.MIDPMediaPlayer;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.controller.ControllerReadyListener;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.epubnav.EPUBNavDocument;
import com.ustadmobile.core.epubnav.EPUBNavItem;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.impl.zip.UMZipEntryInputStream;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import gnu.classpath.java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.json.me.JSONObject;

/**
 * 
 * Implementation of the Container (content e.g. epub) view for J2ME.  This is
 * based on the LWUIT HTMLComponent.  A custom document request handler is used
 * to answer requests and load them directly from the epub zip file.  
 * 
 * Note that ContainerViewPageSplitter is used to split up relatively long 
 * XHTML so that low resource devices do not run out of memory.  Each page
 * is split into sections as required.
 * 
 * As J2ME is resource constrained pages can be divided up into sections using
 * ContainerViewPageSplitter.  
 *
 * @author mike
 */
public class ContainerViewJ2ME extends UstadViewFormJ2ME implements ContainerView, ActionListener, ControllerReadyListener{
        
    private ContainerController controller;
    
    private String title;
    
    private int currentIndex = -1;
    
    private UstadOCF ocf;
    
    private UstadJSOPF opf;
    
    protected ContainerDocumentRequestHandler requestHandler;
    
    private String containerURI;
    
    private String mimeType;
    
    ZipFileHandle containerZip;
    
    private String openContainerBaseURI;
    
    private HTMLComponent htmlC;
    
    /**
     * The OPF base URL within the zip of the container epub that is being shown
     */
    private String opfURL;
    
    private String[] spineURLs;
    
    private Command cmdBack;
    
    private Command cmdForward;
    
    private Command cmdBackToCatalog;
    
    private Command cmdTOC;
    
    private Command cmdOpen;
    
    private Command cmdTOCHide;
    
    private Command[] mainViewCommands;
    
    private Command[] tocCommands;
    
    public static final int CMDBACK_ID = 1;
    
    public static final int CMDFORWARD_ID = 2;
    
    public static final int CMDBACK_TO_CATALOG_ID = 3;
    
    public static final int CMD_TO_TOC_ID = 4;
    
    public static final int CMD_TOC_OPEN_ID = 5;
    
    public static final int CMD_TOC_HIDE = 6;
    
    private ContainerViewHTMLCallback htmlCallback;
    
    static Hashtable mediaExtensions;
    
    long lastPageChangeTime = -1;
    
    /**
     * When a page has been split into sections - this is the index of the current section
     */
    private int sectionIndex;
    
    /**
     * Hashtable in the form of pageURL to a Vector if that page is broken up 
     * into sections.  The vectors contain int[] arrays with two items - where 
     * parsing ended for that section as line number, column number
     */
    private Hashtable sectionBoundaries;
    
    /**
     * The state document for this container for this registration - e.g. as
     * per the Experience API state API
     */
    private JSONObject state;
    
    /**
     * The table of contents list - this is null when the table of contents is
     * not on display
     */
    private List tocList;
    
            
    static {
        mediaExtensions = new Hashtable();
        mediaExtensions.put("mp3", "audio/mpeg");
    }
    
    
    public ContainerViewJ2ME(Hashtable args, Object context) {
        super(args, context, false);
        UstadMobileSystemImplJ2ME impl = UstadMobileSystemImplJ2ME.getInstanceJ2ME();
        containerURI = (String)args.get(ContainerController.ARG_CONTAINERURI);
        mimeType = (String)args.get(ContainerController.ARG_MIMETYPE);
        
        cmdBack = new Command(impl.getString(MessageID.back, getContext()), 
                CMDBACK_ID);
        cmdForward = new Command(impl.getString(MessageID.next, getContext()), 
                CMDFORWARD_ID);
        cmdBackToCatalog = new Command("Exit course", CMDBACK_TO_CATALOG_ID);
        cmdTOC = new Command("Contents", CMD_TO_TOC_ID);
        cmdOpen = new Command("Open", CMD_TOC_OPEN_ID);
        cmdTOCHide = new Command(impl.getString(MessageID.back, getContext()), 
                CMD_TOC_HIDE);
        
        mainViewCommands = new Command[]{cmdForward, cmdBack, cmdBackToCatalog, cmdTOC};
        tocCommands = new Command[]{cmdTOCHide, cmdOpen};
        
        openContainerBaseURI = impl.openContainer(containerURI, mimeType);
        containerZip = impl.getOpenZip();
        setLayout(new BorderLayout());
        
        args.put(ContainerController.ARG_OPENPATH, openContainerBaseURI);
        ContainerController.makeControllerForView(this, args, this);
        
        requestHandler = new ContainerDocumentRequestHandler(this);
        htmlCallback = new ContainerViewHTMLCallback(this);
        
        //make the mediaplayer do vocal logging
        MIDPMediaPlayer player = (MIDPMediaPlayer)DefaultLWUITMediaPlayerManager.getInstance().getPlayer();
        player.setCallback(htmlCallback);
        state = new JSONObject();
        
        setScrollToCompWhenVisible(false);
    }

    public void controllerReady(UstadController controller, int flags) {
        if(controller != null) {
            ContainerController cController = (ContainerController)controller;
            setController(cController);
            htmlCallback.setRegistrationUUID(cController.getRegistrationUUID());
            htmlCallback.setState(state);
            initByContentType();
        }else {
            UstadMobileSystemImpl.l(UMLog.ERROR, 175, containerURI);
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            impl.getAppView(getContext()).showAlertDialog(impl.getString(
                    MessageID.error, getContext()), "Could not open container");
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

    public void setPageTitle(String pageTitle) {
        super.setTitle(pageTitle);
    }
    
    public void show() {
        super.show();
        UstadMobileSystemImplJ2ME.getInstanceJ2ME().handleFormShow(this);
    }

    public boolean isShowing() {
        return isVisible();
    }
    
    public void initByContentType() {
        if(controller.getMimeType().startsWith(UstadJSOPDSItem.TYPE_EPUBCONTAINER)) {
            initEPUB();
        }
    }
    
    private HTMLComponent makeHTMLC() {
        HTMLComponent comp =  new HTMLComponent(requestHandler);
        comp.setHTMLCallback(htmlCallback);
        comp.setImageConstrainPolicy(
            HTMLComponent.IMG_CONSTRAIN_WIDTH | HTMLComponent.IMG_CONSTRAIN_HEIGHT);
        comp.setIgnoreCSS(true);
        comp.setEventsEnabled(true);
        comp.setAutoAddSubmitButton(false);
        comp.setMediaPlayerEnabled(true);
        return comp;
    }
    
    protected void initEPUB() {
        try {
            ocf = controller.getOCF();
            opf = controller.getActiveOPF();
            spineURLs = opf.getLinearSpineHREFs();
                        
            htmlC = makeHTMLC();
                
            //see what we can do about caching
            UMStorageDir[] dirs = UstadMobileSystemImpl.getInstance().getStorageDirs(
                CatalogController.SHARED_RESOURCE, getContext());
            
            long mostSpaceAvailable = 0;
            int bestCacheDir = -1;
            
            long currentSpace;
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            for(int i = 0; i < dirs.length; i++) {
                currentSpace = -1;

                try { 
                    currentSpace = impl.fileAvailableSize(dirs[i].getDirURI());
                    if(currentSpace > mostSpaceAvailable) {
                        mostSpaceAvailable = currentSpace;
                        bestCacheDir = i;
                    }
                }catch(Exception e) {
                    UstadMobileSystemImpl.l(UMLog.WARN, 205, 
                        dirs[i].getDirURI(), e);
                }

            }
            
            if(bestCacheDir != -1) {
                MIDPMediaPlayer.setCacheDir(dirs[bestCacheDir].getDirURI());
                UstadMobileSystemImpl.l(UMLog.INFO, 375, 
                    dirs[bestCacheDir].getDirURI() + " (" + mostSpaceAvailable+" bytes free)");
            }
            
            setCommands(mainViewCommands);
            addCommandListener(this);
            
            opfURL = UMFileUtil.joinPaths(
                    new String[]{UstadMobileSystemImplJ2ME.OPENZIP_PROTO, 
                    ocf.rootFiles[0].fullPath});
            UstadMobileSystemImpl.getInstance().queueTinCanStatement(
                controller.makeLaunchedStatement(), getContext());
            
            Display.getInstance().callSerially(new Runnable() {
                public void run() {
                    addComponent(BorderLayout.CENTER, htmlC);
                    showPage(0, 0);
                }
            });
        }catch(Exception e) {
            UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 350, null, e);
        }
    }
    
    private void setCommands(Command[] cmds) {
        removeAllCommands();
        for(int i = 0; i < cmds.length; i++) {
            addCommand(cmds[i]);
        }
    }
    
    /**
     * Return the full tincan id of the current page
     * 
     * @return 
     */
    public String getCurrentTinCanPageID() {
        return UMFileUtil.joinPaths(new String[]{getContainerTinCanID(), 
            getCurrentTinCanPageID()});
    }
    
    public String getContainerTinCanID() {
        return controller.getBaseTinCanId();
    }
    
    public String getCurrentPageOPFId() {
        UstadJSOPF opf = controller.getActiveOPF();
        return opf.spine[currentIndex].id;
    }
    
    public JSONObject getTinCanContext() {
        return controller.getTinCanContext();
    }
    
    /**
     * The state document for this container for this registration - e.g. as
     * per the Experience API state API
     * 
     * This doesn't yet send itself to the xAPI master server but it could
     * do one day
     * 
     * @return JSONObject representing the state of the current registration
     */
    public JSONObject getState() {
        return state;
    }
    
    
    
    /**
     * The main HTML Component showing the content
     * 
     * @return LWUIT HTML Component used to show content
     */
    public HTMLComponent getHtmlComponent() {
        return this.htmlC;
    }
    
    /**
     * Make a tincan statement about the user viewing the current page and
     * send it to the system implementation to be queued.
     */
    protected void makePageStatement() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(lastPageChangeTime != -1 && impl.getActiveUser(getContext()) != null) {
            long duration =  System.currentTimeMillis() - lastPageChangeTime;
            JSONObject actor = UMTinCanUtil.makeActorFromActiveUser(getContext());
            String currentPage = spineURLs[currentIndex];
            
            //TODO: get language of the page itself or from OPF
            String title = currentPage;
            if(htmlC != null && htmlC.getTitle() != null) {
                title = htmlC.getTitle();
            }
            
            String tinCanId = UMFileUtil.joinPaths(new String[] {
                UstadMobileDefaults.DEFAULT_TINCAN_PREFIX, opf.id, currentPage});
            JSONObject stmt = UMTinCanUtil.makePageViewStmt(tinCanId,
                title, "en-US", duration, actor);
            
            //TODO: Temporarily disabled whilst checking memory consumption
            //UstadMobileSystemImpl.getInstance().queueTinCanStatement(stmt, getContext());
        }
    }
    
   
    /**
     * Show the page as per the index in the spineURLs
     * 
     * @param pageIndex Index of page number to show
     * @param sectionNum index of the section chunk to show
     */
    public void showPage(int pageIndex, int sectionNum) {
        if(pageIndex == currentIndex && sectionNum == sectionIndex) {
            return;
        }
        makePageStatement();
        htmlCallback.beforeUnload(htmlC);
        
        DefaultLWUITMediaPlayerManager.getInstance().getPlayer().stopAllPlayers(true);
                
        String pageURL = UMFileUtil.resolveLink(opfURL, spineURLs[pageIndex]);
        if(sectionNum != 0) {
            int[] startFrom = (int[])getSectionBoundaries(pageURL).elementAt(sectionNum-1);
            pageURL += "?s-index=" + sectionNum + "&startline=" + startFrom[0] + 
                "&startcol=" + startFrom[1];
        }
        
        htmlC.setPage(pageURL);
        this.currentIndex = pageIndex;
        this.sectionIndex = sectionNum;
        lastPageChangeTime = System.currentTimeMillis();
    }
    
    /**
     * Uses LoadNavDocTask to load the table of contents from the navigation 
     * document and show them.
     */
    public void showTOC() {
        //Change me to not run this on the main thread
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(getContext()).showProgressDialog(impl.getString(
                MessageID.loading, getContext()));
        requestHandler.schedule(new LoadNavDocTask(), 50);
    }
    
    /**
     * Called by the LoadNavDocTask once the toc document has been parsed
     * 
     * @param navDoc 
     */
    public void handleNavDocReady(EPUBNavDocument navDoc) {
        final EPUBNavItem tocItem = navDoc != null ? navDoc.getNavById("toc") : null;
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final ContainerViewJ2ME view = this;
        impl.getAppView(getContext()).dismissProgressDialog();
        Display.getInstance().callSerially(new Runnable() {
            public void run() {
                if(tocItem != null) {
                    Vector tocItems = tocItem.getChildrenRecursive(new Vector());
                    tocList = new List(tocItems);
                    tocList.addActionListener(view);

                    int selectIndex = EPUBNavItem.findItemInVectorByHref(
                        spineURLs[currentIndex], tocItems);
                    
                    removeComponent(htmlC);
                    addComponent(BorderLayout.CENTER, tocList);
                    setFocused(tocList);
                    
                    if(selectIndex != -1) {
                        tocList.setSelectedIndex(selectIndex);
                    }
                    setCommands(tocCommands);
                }
            }
        });
        
    }
    
    /**
     * Hide the table of contents
     * 
     * @param index 
     */
    public void hideTOC() {
        removeComponent(tocList);
        addComponent(BorderLayout.CENTER, htmlC);
        tocList = null;
        setCommands(mainViewCommands);
    }
    
    public void openSelectedTOCEntry() {
        Object selectedObj = tocList.getSelectedItem();
        hideTOC();
        if(selectedObj != null) {
            EPUBNavItem selectedItem = (EPUBNavItem)selectedObj;
            String href = selectedItem.href;//TODO: remove any anchor text after #
            int spinePos = opf.getLinearSpinePositionByHREF(href);
            showPage(spinePos, 0);
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == tocList) {
            openSelectedTOCEntry();
            return;
        }
        
        int cmdId = ae.getCommand() != null ? ae.getCommand().getId() : -1;
        switch(cmdId) {
            case CMDBACK_ID:
            case CMDFORWARD_ID:
                int dir = cmdId == CMDBACK_ID ? -1 : 1;
                go(dir);
                break;

            case CMDBACK_TO_CATALOG_ID:
                DefaultLWUITMediaPlayerManager.getInstance().getPlayer().stopAllPlayers(true);
                UstadMobileSystemImplJ2ME.getInstanceJ2ME().goBack(getContext());
                break;

            case CMD_TO_TOC_ID:
                showTOC();
                break;
                
            case CMD_TOC_HIDE:
                hideTOC();
                break;
                
            case CMD_TOC_OPEN_ID:
                openSelectedTOCEntry();
                break;
        }
    }
    
    /**
     * Moves along the content from one section to the next, and if there is
     * no section left in the page moves to the next page / previous page
     * 
     * @param dir 
     */
    public void go(int dir) {
        String pageURL = UMFileUtil.resolveLink(opfURL, 
            spineURLs[this.currentIndex]);
        int pageIndex = -1;
        int sectionNum = -1;

        /*
         * Check and see if there are page sections on this page for
         * back/next as requested
         */
        if(getSectionBoundaries(pageURL) != null) {
            Vector pgSections = getSectionBoundaries(pageURL);
            int[] currentBoundaries = (int[])pgSections.elementAt(this.sectionIndex);

            if(dir == 1 && currentBoundaries[0] >= 0 || dir == -1 && sectionIndex > 0) {
                pageIndex = this.currentIndex;
                sectionNum = this.sectionIndex+dir;
            }
        }

        /*
         * If there is not an appropriate section within the current page 
         * to go to
         */
        if(pageIndex == -1){
            pageIndex = this.currentIndex+dir;
            //see if that page is also broken into sections
            if(pageIndex >= 0 && pageIndex < spineURLs.length) {
                pageURL = UMFileUtil.resolveLink(opfURL, 
                    spineURLs[pageIndex]);

                if(dir == 1 || getSectionBoundaries(pageURL) == null) {
                    sectionNum = 0;
                }else {
                    sectionNum = getSectionBoundaries(pageURL).size()-1;
                }
            }
        }

        if(pageIndex >= 0 && pageIndex < spineURLs.length) {
            showPage(pageIndex, sectionNum);
        }
    }
    

    public void setContainerTitle(String containerTitle) {
        setTitle(containerTitle);
    }
    
    /**
     * Method to be called from the HTMLCallback when a new page is loaded -
     * thus keeping the spine position tracker updated
     * 
     * @param newURL : the page that is in the process of being loaded (absolute url)
     */
    protected void handlePageChange(String newURL) {
        String relativeTo = UMFileUtil.getParentFilename(this.opfURL);
        if(relativeTo.charAt(relativeTo.length()-1) != '/') {
            relativeTo += '/';
        }
        
        String spineHREF = newURL.substring(relativeTo.length());
        int spinePos = opf.getLinearSpinePositionByHREF(spineHREF);
        if(spinePos != -1 && spinePos != currentIndex) {
            this.currentIndex = spinePos;
        }
    }

    public void onDestroy() {
        if(openContainerBaseURI != null) {
            UstadMobileSystemImplJ2ME impl = UstadMobileSystemImplJ2ME.getInstanceJ2ME();
            impl.closeContainer(openContainerBaseURI);    
            openContainerBaseURI = null;
        }
        
        if(requestHandler != null && requestHandler.timer != null) {
            requestHandler.stopTimer();
        }
    }

    public void showPDF() {
        //not supported on J2ME
    }

    public void showEPUB() {
        //everything is an epub really on J2ME
    }

    public boolean refreshURLs() {
        return true;//we don't handle registrations using URL params on J2ME
    }
    
    /**
     * JSON Object in the form of:
     * 
     * {
     *   "0" : [ [0,0], [5, 10]]
     * }
     * 
     * Where:
     *  "0" a string representation of the index of the page in the spine
     *  The array of arrays is in the form of section boundaries - each as [line, col]
     */
    public Hashtable getSectionBoundaries() {
        return sectionBoundaries;
    }
    
    
    public Vector getSectionBoundaries(String pageURL) {
        if(sectionBoundaries != null && sectionBoundaries.containsKey(pageURL)) {
            return (Vector)sectionBoundaries.get(pageURL);
        }else {
            return null;
        }
    }
    
    public void setSectionBoundaries(String pageURL, Vector boundaries) {
        if(sectionBoundaries == null) {
            sectionBoundaries = new Hashtable();
        }
        
        sectionBoundaries.put(pageURL, boundaries);
    }
    
    
    /**
     * Handles requests to load resources by pointing them to the openzip and
     * requests to play media
     * 
     * To run non sync: 
     * public static class ContainerDocumentRequestHandler implements DocumentRequestHandler {
     * async:
     * public static class ContainerDocumentRequestHandler implements AsyncDocumentRequestHandler {
     */
    public static class ContainerDocumentRequestHandler implements AsyncDocumentRequestHandler {

        private ContainerViewJ2ME view;
        
        protected Timer timer;
        
        public ContainerDocumentRequestHandler(ContainerViewJ2ME view) {
            this.view = view;
            startTimer();
        }
        
        /**
         * Creates / starts the timer if it does not already exist.  Calling this
         * multiple times will have no  effect
         */
        protected void startTimer() {
            if(timer == null) {
                timer = new Timer();
            }
        }
        
        protected void stopTimer() {
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
        }
        
        protected void schedule(TimerTask task, long delay) {
            startTimer();
            timer.schedule(task, delay);
        }
        
        /**
         * Buffer is enabled for all requests except audio and video media
         * 
         * @param di
         * 
         * @return 
         */
        boolean shouldBeBufferEnabled(DocumentInfo di) {
            if(di == null) {
                return true;
            }
            
            int type = di.getExpectedContentType();
            return (type != DocumentInfo.TYPE_AUDIO) && 
                (type != DocumentInfo.TYPE_VIDEO);
        }
        
        public InputStream resourceRequested(DocumentInfo di) {
            
            return resourceRequested(di.getUrl(), di.getExpectedContentType(), 
                shouldBeBufferEnabled(di), di);
        }
        
        public InputStream resourceRequested(String requestURL, int expectedType, boolean bufferEnabled, DocumentInfo di) {
            InputStream src;
            try {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 603, requestURL);
                if(expectedType != DocumentInfo.TYPE_IMAGE && di != null) {
                    di.setEncoding(DocumentInfo.ENCODING_UTF8);
                }
                
                String pathInZip = requestURL.substring(
                    UstadMobileSystemImplJ2ME.OPENZIP_PROTO.length());
                int queryPos = pathInZip.indexOf('?');
                String urlQuery = null;
                
                if(queryPos != -1) {
                    pathInZip = pathInZip.substring(0, queryPos);
                    urlQuery = requestURL.substring(requestURL.indexOf('?')+1);
                }
                
                src = view.containerZip.openInputStream(pathInZip);
                
                int entrySize = (int)((UMZipEntryInputStream)src).getZipEntry().getSize();
                
                //If we are getting HTML: split the page into chunks
                if(di != null && di.getExpectedContentType() == DocumentInfo.TYPE_HTML) {
                    String pageURL = requestURL;
                    if(pageURL.indexOf('?') != -1) {
                        pageURL = pageURL.substring(0, pageURL.indexOf('?'));
                    }

                    Vector boundaries = view.getSectionBoundaries(pageURL);
                    if(boundaries == null) {
                        boundaries = new Vector();
                    }

                    //look through the URL params to see which section to use
                    int index = 0;
                    int line = 0;
                    int col = 0;
                    Hashtable params = null;

                    if(urlQuery != null) {
                        params = UMFileUtil.parseURLQueryString(urlQuery);
                    }
                    
                    if(params == null || !params.containsKey("nosplit")) {
                        if(params != null && params.containsKey("s-index")) {
                            index = Integer.parseInt((String)params.get("s-index"));
                        }

                        if(params != null && params.containsKey("startline")) {
                            line = Integer.parseInt((String)params.get("startline"));
                            col = Integer.parseInt((String)params.get("startcol"));
                        }

                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        try {
                            int[] endingPos = ContainerViewPageSplitter.dividePage(src, bout, 
                                UstadMobileConstants.MICRO_ED_PAGESPLIT_TEXTLEN, 
                                Display.getInstance().getDisplayWidth() * Display.getInstance().getDisplayHeight() * 4,
                                line, col);

                            if(index == boundaries.size()) {
                                //we need to add this to the vector
                                boundaries.addElement(endingPos);
                                view.setSectionBoundaries(requestURL, boundaries);
                            }

                            byte[] bytes = bout.toByteArray();
                            //String pageStr = new String(bytes, "UTF-8");
                            src.close();

                            src = new ByteArrayInputStream(bytes);
                            di.setContentLength(bytes.length);
                        }catch(Exception xe) {
                            UstadMobileSystemImpl.l(UMLog.ERROR, 198, null, xe);
                            throw new IOException(xe.toString());
                        }finally {
                            bout = null;
                        }
                    }
                }
                
                //see if we know what the size is...
                if(di != null && src instanceof UMZipEntryInputStream) {
                    di.setContentLength(entrySize);
                }
                
                
                if(src instanceof ByteArrayInputStream) {
                    return src;
                }else if(bufferEnabled) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    return J2MEIOUtils.readToByteArrayStream(src);
                }else {
                    return new BufferedInputStream(src, 20*1024);
                }
                
            }catch(Exception e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 199, null, e);
                return new ByteArrayInputStream("ERROR".getBytes());
            }
        }

        public void resourceRequestedAsync(String url, IOCallback ioc, boolean bufferEnabled, int expectedType) {
            schedule(new ContainerDocumentRequestTask(url, ioc, this, 
                bufferEnabled, expectedType), 50);
        }
        
        public void resourceRequestedAsync(DocumentInfo di, IOCallback ioc, boolean bufferEnabled) {
            schedule(new ContainerDocumentRequestTask(di, ioc, this, 
                bufferEnabled), 50);
        }
        
        public void resourceRequestedAsync(DocumentInfo di, IOCallback ioc) {
            resourceRequestedAsync(di, ioc, shouldBeBufferEnabled(di));
        }
        
    }
    
    /**
     * TimerTask to open resources 
     * 
     */
    public static class ContainerDocumentRequestTask extends TimerTask {

        private DocumentInfo di;
        
        private IOCallback ioc;
        
        private ContainerDocumentRequestHandler handler;
        
        private boolean bufferEnabled;
        
        private String url;
        
        private int expectedType;
        
        public ContainerDocumentRequestTask(DocumentInfo di, IOCallback ioc, ContainerDocumentRequestHandler handler, boolean bufferEnabled) {
            this.di = di;
            this.ioc = ioc;
            this.handler = handler;
            this.bufferEnabled = bufferEnabled;
                  
            url = di.getUrl();
            expectedType = di.getExpectedContentType();
        }
        
        public ContainerDocumentRequestTask(String url, IOCallback ioc, ContainerDocumentRequestHandler handler, boolean bufferEnabled, int expectedType) {
            this.url = url;
            this.ioc = ioc;
            this.bufferEnabled = bufferEnabled;
            this.expectedType = expectedType;
            this.di = null;
            this.handler = handler;
        }
        
        public void run() {
            InputStream in = handler.resourceRequested(url, expectedType, bufferEnabled, di);
            ioc.streamReady(in, di);
        }
    }
    
    /**
     * Timer task that runs on the background using ContainerDocumentRequestHandler's
     * timer.
     */
    public class LoadNavDocTask extends TimerTask {
        public void run() {
            InputStream in = null;
            EPUBNavDocument navDoc = null;
            try {
                in = requestHandler.resourceRequested(
                    new DocumentInfo(UMFileUtil.resolveLink(opfURL, 
                        ContainerViewJ2ME.this.opf.navItem.href) + "?nosplit=true", 
                        DocumentInfo.TYPE_HTML));
                navDoc = EPUBNavDocument.load(in);
            }catch(Exception e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 1, title);
            }finally {
                UMIOUtils.closeInputStream(in);
            }
            
            ContainerViewJ2ME.this.handleNavDocReady(navDoc);
        }
    }
    

}
