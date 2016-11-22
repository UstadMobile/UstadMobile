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

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.ocf.UstadOCF;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.opf.UstadJSOPF;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.ContainerView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.tincan.Registration;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.UstadView;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;



/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */


/**
 * Represents a container (e.g. epub file)
 * 
 * 
 * @author mike
 */
public class ContainerController extends UstadBaseController implements AsyncLoadableController, TinCanResultListener, AppViewChoiceListener{
    
    private ContainerView containerView;
    
    private String openPath;
        
    private String mimeType;
    
    private UstadJSOPDSEntry entry;
    
    private UstadOCF ocf;
    
    private UstadJSOPF activeOPF;
    
    private String[] opfTitles;
    
    private String registrationUUID;
    
    private TinCanXML tinCanXMLSummary;

    private String currentPageTitle;
    
    public static final String PREFKEY_PREFIX_LASTOPENED = "laxs-";
        
    /**
     * Use with loadController as the key for the containerURI in args hashtable
     * @see ContainerController#loadController(java.util.Hashtable) 
     */
    public static final String ARG_CONTAINERURI = "URI";
    
    /**
     * Use with loadController as the key for where the contents of a zip
     * file can actually be accessed e.g. over an HTTP mount etc.
     */
    public static final String ARG_OPENPATH = "OPATH";
    
    /**
     * Use with loadController as the key for the mime type in args hashtable
     * @see ContainerController#loadController(java.util.Hashtable) 
     */
    public static final String ARG_MIMETYPE = "MIME";
    
    /**
     * Use with loadController as the key for the OPF index to load from the
     * container if this container is an EPUB file
     */
    public static final String ARG_OPFINDEX = "OPFI";
    
    /**
     * Use with loadController to specify the registration UUID if required
     * (e.g. if the Mobile OS has destroyed the activity and it is being
     * re-created)
     */
    public static final String ARG_XAPI_REGISTRATION = "XAPI_REG";
    
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
    
    
    public static final int CMD_RESUME_SESSION = 1011;
    
    public static final int CMD_CHOOSE_SESSION = 1012;
    
    /**
     * After searching for resumable registrations: these are the ones that can 
     * be resumed
     */
    private String[] resumableRegistrationIds;
    
    
    
    /**
     * Empty constructor - this creates a blank unusable object - required for async loading
     */
    public ContainerController(Object context) {
         super(context);
    }
    
    public static void makeControllerForView(ContainerView view, Hashtable args, ControllerReadyListener listener) {
        ContainerController ctrl = new ContainerController(view.getContext());
        new LoadControllerThread(args, ctrl, listener, view).start();
    }
    
    /**
     * Gets the currently active OPF for this controller (used if this represents
     * an epub file)
     * 
     * @return Currently active OPF object
     */
    public UstadJSOPF getActiveOPF() {
        return activeOPF;
    }

    /**
     * Sets the currently active OPF for this container (used if this represents
     * an epub file)
     * 
     * @param activeOPF The currently active OPF
     */
    public void setActiveOPF(UstadJSOPF activeOPF) {
        this.activeOPF = activeOPF;
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
    
    /**
     * The path that is being used to access the resource.  On many platforms
     * we need to use an internal HTTP server etc. So this is the internal
     * HTTP server URL path to the root of the container.
     * 
     * @return 
     */
    public String getOpenPath() {
        return openPath;
    }
    
    /**
     * The directory in which a given OPF is; 
     * 
     * e.g. If the open path for the container is http://server:1234/container
     * and the given OPF files comes from EPUB/package.opf within the container
     * this will return http://server:1234/container/EPUB
     * 
     * @return The directory from which the given opf has been opened
     */
    public String getOPFBasePath(UstadJSOPF opf){
        int opfIndex = 0;//TODO: this should select the index as per the OPF arg
        return UMFileUtil.getParentFilename(UMFileUtil.joinPaths(
            new String[] {openPath, ocf.rootFiles[opfIndex].fullPath}));
    }
   
    
    public String getMimeType() {
        return mimeType;
    }
    
    public UstadView getView() {
        return containerView;
    }
    
    public void setView(UstadView view) {
        this.containerView = (ContainerView)view;
        super.setView(view);
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
        
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String containerXMLURI = UMFileUtil.joinPaths(
                new String[]{openPath, "META-INF/container.xml"});
        
        HTTPResult res = UstadMobileSystemImpl.getInstance().readURLToString(
            containerXMLURI, null);

        XmlPullParser xpp = impl.newPullParser();
        xpp.setInput(new ByteArrayInputStream(res.getResponse()), "UTF-8");
        ocf = UstadOCF.loadFromXML(xpp);
        impl.getLogger().l(UMLog.DEBUG, 534, "Got ocf");
        
        opfTitles = new String[ocf.rootFiles.length];
        impl.getLogger().l(UMLog.DEBUG, 534, null);
        
        return ocf;
    }
    
    /**
     * 
     * @param flags
     * @return
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public TinCanXML getTinCanXML(int flags) throws IOException, XmlPullParserException {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String tinCanXMLURI = UMFileUtil.joinPaths(
                new String[]{openPath, "tincan.xml"});
        TinCanXML tcXML = null;
        
        try {
            HTTPResult res = impl.getInstance().readURLToString(tinCanXMLURI, null);
            if(res != null && res.getStatus() == 200) {
                XmlPullParser xpp = impl.newPullParser();
                xpp.setInput(new ByteArrayInputStream(res.getResponse()), "UTF-8");
                tcXML = TinCanXML.loadFromXML(xpp);
            }else {
                UstadMobileSystemImpl.l(UMLog.WARN, 2, openPath);
            }
        }catch(IOException e) {
            //seems we don't have that...
            UstadMobileSystemImpl.l(UMLog.WARN, 211, null, e);
        }
        
        return tcXML;
    }
    
    /**
     * Returns a TinCanXML summary object that contains only the info on the
     * activity with a launch element (if any).  To conserve memory we don't
     * preserve info on all activities that may be contained in the tincan.xml
     * 
     * @return TinCanXML summary object as above
     * 
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public TinCanXML getTinCanXMLSummary() throws IOException, XmlPullParserException{
        if(tinCanXMLSummary != null) {
            return tinCanXMLSummary;
        }
        
        tinCanXMLSummary = getTinCanXML(0);
        return tinCanXMLSummary;
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
     * Make an array of the spine URLs using the currently active OPF.
     * 
     * @param addXAPIParams If true will add XAPI parameters to the end of the each URL
     * 
     * @return Array of URLs with resolved relative to the baseURL and each with the added xAPI parameters
     * @throws IOException
     * @throws XmlPullParserException 
     */
    public String[] getSpineURLs(boolean addXAPIParams) throws IOException, XmlPullParserException{
        return resolveHREFS(activeOPF, getActiveOPF().getLinearSpineHREFs(), 
            addXAPIParams ? getXAPIQuery() : null);
    }
    
    /**
     * Returns a Query String for the xAPI parameters for the container including 
     * the tincan actor, authorization, endpoint, and registration UUID
     * 
     * @return Query string as above
     */
    public String getXAPIQuery() {
        String username = UstadMobileSystemImpl.getInstance().getActiveUser(getContext());
        String password = UstadMobileSystemImpl.getInstance().getActiveUserAuth(getContext());

        //TODO: Change this hardcoded setting to something that is set properly
        return "?actor=" +
            URLTextUtil.urlEncodeUTF8(UMTinCanUtil.makeActorFromActiveUser(getContext()).toString()) +
            "&auth=" + URLTextUtil.urlEncodeUTF8(LoginController.encodeBasicAuth(username, password)) +
            "&endpoint=" + URLTextUtil.urlEncodeUTF8("http://127.0.0.1:8001/xapi/") +
            "&registration=" + registrationUUID;
    }
    
    
    /**
     * Resolve an HREF from an OPF item to a full path
     * 
     * @param opf
     * @param href
     * 
     * @return 
     */
    public String[] resolveHREFS(UstadJSOPF opf, String hrefs[], String postfix) {
        String[] resolved = null;
        try {
            resolved = new String[hrefs.length];
            String opfPath = UMFileUtil.joinPaths(new String[]{openPath, 
                getOCF().rootFiles[0].fullPath});
            for(int i = 0; i < hrefs.length; i++) {
                resolved[i] = UMFileUtil.resolveLink(opfPath, hrefs[i]);
                if(postfix != null) {
                    resolved[i] += postfix;
                }
            }
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 168, opf.id, e);
        }
        
        return resolved;
    }
    
    
    
    /**
     * Generate a launched statement for this course
     * 
     * @return 
     */
    public JSONObject makeLaunchedStatement() {
        JSONObject stmt = null;
        
        try {
            stmt = new JSONObject();
            
            if(this.tinCanXMLSummary != null && this.tinCanXMLSummary.getLaunchActivity() != null) {
                stmt.put("object", this.tinCanXMLSummary.getLaunchActivity().getActivityJSON());
            }else {
                JSONObject objectObj = new JSONObject();
                objectObj.put("id", "epub:" + activeOPF.id);
                stmt.put("object", objectObj);
            }
            
            stmt.put("actor", UMTinCanUtil.makeActorFromActiveUser(getContext()));
            
            JSONObject verbDef = new JSONObject();
            verbDef.put("id", "http://adlnet.gov/expapi/verbs/launched");
            stmt.put("verb", verbDef);
            stmt.put("context", getTinCanContext());
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 190, null, e);
        }
        
        return stmt;
    }
    
    /**
     * Retrieves the TinCan ID that's being used as the base ID of this container
     * 
     * If tincan.xml is used this means the activity which contains a launch
     * element.  Otherwise we'll use epub:opfId for an EPUB file
     * 
     * Child activities should be in the form of baseId/PageId/ideviceId
     * 
     * @return The base tincan ID for this container as above
     */
    public String getBaseTinCanId() {
        if(this.tinCanXMLSummary != null && this.tinCanXMLSummary.getLaunchActivity() != null) {
            return this.tinCanXMLSummary.getLaunchActivity().getId();
        }else {
            return "epub:" + activeOPF.id;
        }
    }
    
    /**
     * Make a JSON Object for the TinCan context object with the for the given
     * registration 
     * 
     * @param registrationUUID Registration UUID for the context as per xAPI spec
     * @return JSON Object with the registration property set
     */
    public static JSONObject makeTinCanContext(String registrationUUID) {
        JSONObject context = null;
        try {
            context = new JSONObject();
            context.put("registration", registrationUUID);
        }catch(Exception e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 189, null, e);
        }
        
        return context;
    }
    
    /**
     * Get a JSON Object representing the TinCan context of the container
     * Really this is just here to put in the registration
     * 
     * @return 
     */
    public JSONObject getTinCanContext() {
        return makeTinCanContext(this.registrationUUID);
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
     * Load this controller - used by the async thread basesd loader
     * 
     * @param args should include the containeruri and mimetype as per ARG constants
     * @return
     * @throws Exception 
     * @see ContainerController#ARG_CONTAINERURI
     * @see ContainerController#ARG_MIMETYPE
     */
    public UstadController loadController(Hashtable args, Object context) throws Exception {
        openPath = (String)args.get(ARG_OPENPATH);
        mimeType = (String)args.get(ARG_MIMETYPE);
        if(mimeType.startsWith(UstadJSOPDSItem.TYPE_EPUBCONTAINER)) {
            getOCF();
            if(args.containsKey(ARG_OPFINDEX)) {
                setActiveOPF(getOPF(
                    ((Integer)args.get(ARG_OPFINDEX)).intValue()));
            }
        }
        
        TinCanXML tc = getTinCanXMLSummary();
        
        if(args.containsKey(ARG_XAPI_REGISTRATION)) {
            registrationUUID = (String)args.get(ARG_XAPI_REGISTRATION);
        }else {
            registrationUUID = UMTinCanUtil.generateUUID();
        }
        
        return this;
    }
    
    /**
     * Returns true if this content type supports resumable registrations: false otherwise
     * 
     * @return 
     */
    public boolean supportsResumableRegistrations() {
        return this.tinCanXMLSummary != null && this.tinCanXMLSummary.getLaunchActivity() != null;
    }
    
    /**
     * Should be called by the view when the user selects the resume menu item
     */
    public void handleClickResumableRegistrationMenuItem() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        if(supportsResumableRegistrations()) {
            impl.getAppView(getContext()).showProgressDialog(impl.getString(MessageIDConstants.loading));
            getResumableRegistrations(this);
        }else {
            UstadMobileSystemImpl.getInstance().getAppView(getContext()).showAlertDialog(
                "Ooops", "Sorry: No resumable sessions for this item");
        }
    }
    
    /**
     * Handle when the TinCan Result is ready 
     * @param result 
     */
    public void resultReady(Object result) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.getAppView(getContext()).dismissProgressDialog();
        Vector regIds = new Vector();
        Vector labels = new Vector();
        if(result != null) {
            JSONObject[] openSessions = (JSONObject[])result;
            StringBuffer labelSb;
            String regId;
            Calendar cal;
            for(int i = 0; i < openSessions.length; i++) {
                try {
                    regId = openSessions[i].getJSONObject("context").getString("registration");
                    cal = UMTinCanUtil.parse8601Timestamp(
                        openSessions[i].getString("timestamp"));
                    labelSb = new StringBuffer();
                    labelSb.append(cal.get(Calendar.DAY_OF_MONTH)).append('/');
                    labelSb.append(cal.get(Calendar.MONTH)+1).append('/');
                    labelSb.append(cal.get(Calendar.YEAR)).append(" - ");
                    labelSb.append(cal.get(Calendar.HOUR_OF_DAY)).append(':');
                    labelSb.append(UMUtil.pad0(cal.get(Calendar.MINUTE)));
                    
                    regIds.addElement(regId);
                    labels.addElement(labelSb.toString());
                }catch(JSONException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 196, null, e);
                }
            }
            
            if(labels.size() > 0) {
                resumableRegistrationIds = new String[regIds.size()];
                regIds.copyInto(resumableRegistrationIds);
                String[] labelArr = new String[labels.size()];
                labels.copyInto(labelArr);
                impl.getAppView(getContext()).showChoiceDialog("Resume", labelArr, 
                    CMD_CHOOSE_SESSION, this);
            }else {
                impl.getAppView(getContext()).showAlertDialog("No sessions", 
                    "No resumable sessionss avaialble");
            }
        }else {
            impl.getAppView(getContext()).showAlertDialog("Error",
                "Sorry - error fetching resumable sessions");
        }
    }
    


    /**
     * Handle when the user has chosen a session to resume
     * 
     * @param commandId
     * @param choice 
     */
    public void appViewChoiceSelected(int commandId, int choice) {
        switch(commandId) {
            case CMD_CHOOSE_SESSION:
                registrationUUID = resumableRegistrationIds[choice];
                containerView.refreshURLs();
                UstadMobileSystemImpl.getInstance().getAppView(getContext()).showNotification(
                    "Loaded session", AppView.LENGTH_LONG);
                break;
        }
    }
    
    

    
    public int getResultType() {
        return 1;
    }
    
    /**
     * Get a list of resuamble registrations (if any) for this container
     * 
     * @return Array of registration objects that are resumable registrations
     * 
     * @throws IOException 
     */
    public void getResumableRegistrations(TinCanResultListener listener) {
        //TODO: Add check that this is a resuambel act
        if(supportsResumableRegistrations()) {
            UstadMobileSystemImpl.getInstance().getResumableRegistrations(
                this.tinCanXMLSummary.getLaunchActivity().getId(), getContext(), listener);
        }
    }
    
    
    /**
     * For the given array of registrations: make an array of the labels to use
     * for a dialog
     * 
     * @param registrations
     * @return 
     */
    public String[] getRegistrationLabels(Registration[] registrations) {
        String[] str = new String[registrations.length];
        for(int i = 0; i < str.length; i++) {
            str[i] = registrations[i].uuid;
        }
        
        return str;
    }
    
    public void setUIStrings() {
        int[] cmds = new int[STANDARD_APPEMNU_CMDS.length + 1];
        String[] labels = new String[STANDARD_APPEMNU_CMDS.length + 1];
        cmds[0] = CMD_RESUME_SESSION;
        labels[0] = "Resume";
        
        super.fillStandardMenuOptions(cmds, labels, 1);
        getView().setAppMenuCommands(labels, cmds);
    }
    
    /**
     * Gets the current registration UUID that is being used for the container.
     * 
     * This is generated at random or loaded from a saved value and handled
     * when loadController runs.
     * 
     * @return XAPI Registration UUID as a String
     */
    public String getRegistrationUUID() {
        return registrationUUID;
    }
    
    /**
     * Sets the current registration UUID to be used
     */
    public void setRegistrationUUID(String registrationUUID) {
        this.registrationUUID = registrationUUID;
    }

    public void handlePageTitleUpdated(String pageTitle) {
        this.currentPageTitle = pageTitle;
        if(containerView != null) {
            containerView.setPageTitle(pageTitle);
        }
    }
    
    
}
