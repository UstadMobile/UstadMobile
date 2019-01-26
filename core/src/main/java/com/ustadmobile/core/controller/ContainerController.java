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

import com.ustadmobile.core.contentformats.epub.nav.EpubNavDocument;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.ContainerView;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.impl.UmCallback;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.json.*;


/**
 * Represents a container (e.g. epub file)
 * 
 * 
 * @author mike
 */
public class ContainerController extends UstadBaseController {
    
    private ContainerView containerView;

    private OcfDocument ocf;
    
    private OpfDocument activeOPF;

    private String registrationUUID;
    
    private TinCanXML tinCanXMLSummary;

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
     * Use with loadController as the key for the OPF index to load from the
     * container if this container is an EPUB file
     */
    public static final String ARG_OPFINDEX = "OPFI";

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



    private String mountedUrl;

    private UmHttpCall containerXmlCall;

    private UmCallback mountedCallbackHandler = new UmCallback() {

        @Override
        public void onSuccess(Object result) {
            mountedUrl = (String) result;
            String containerUri = UMFileUtil.joinPaths(mountedUrl, OCF_CONTAINER_PATH);
            containerXmlCall = UstadMobileSystemImpl.getInstance().makeRequestAsync(
                    new UmHttpRequest(getContext(), containerUri), containerHttpCallbackHandler);
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmHttpResponseCallback containerHttpCallbackHandler = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            if(response.isSuccessful()) {
                ocf = new OcfDocument();

                try {
                    XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                        new ByteArrayInputStream(response.getResponseBody()));
                    ocf.loadFromParser(xpp);

                    //get and parse the first publication
                    String opfUrl = UMFileUtil.joinPaths(mountedUrl,
                            ocf.getRootFiles().get(0).getFullPath());
                    UstadMobileSystemImpl.getInstance().makeRequestAsync(
                            new UmHttpRequest(getContext(), opfUrl), opfHttpCallbackHandler);

                }catch(IOException e) {
                    e.printStackTrace();
                }catch(XmlPullParserException x) {
                    x.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 500, "Exception loading container");
        }
    };

    private UmHttpResponseCallback opfHttpCallbackHandler = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            OpfDocument opf = new OpfDocument();
            try {
                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                        new ByteArrayInputStream(response.getResponseBody()));
                opf.loadFromOPF(xpp);
                final String[] linearSpineHrefs = opf.getLinearSpineHREFs();
                final String baseUrl = UMFileUtil.getParentFilename(UMFileUtil.joinPaths(
                        mountedUrl, ocf.getRootFiles().get(0).getFullPath()));
                final String xapiQuery = getXAPIQuery(UMFileUtil.resolveLink(mountedUrl, "/xapi/"));
                final String containerTitle = opf.title;
                final OpfItem opfCoverImageItem = opf.getCoverImage(null);
                final String authorNames = opf.getNumCreators() > 0 ?
                        UMUtil.joinStrings(opf.getCreators(), ", ") : null;

                containerView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        containerView.setContainerTitle(containerTitle);
                        containerView.setSpineUrls(baseUrl, linearSpineHrefs, xapiQuery);
                        if(opfCoverImageItem != null) {
                            containerView.setCoverImage(UMFileUtil.resolveLink(baseUrl,
                                    opfCoverImageItem.href));
                        }

                        if(authorNames != null) {
                            containerView.setAuthorName(authorNames);
                        }
                    }
                });

                if(opf.getNavItem() == null)
                    return;

                String navXhtmlUrl = UMFileUtil.resolveLink(UMFileUtil.joinPaths(
                        mountedUrl, ocf.getRootFiles().get(0).getFullPath()), opf.getNavItem().href);

                UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(
                        getContext(), navXhtmlUrl), navCallbackHandler);
            }catch(IOException e) {
                e.printStackTrace();
            }catch(XmlPullParserException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {

        }
    };

    private UmHttpResponseCallback navCallbackHandler = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            final EpubNavDocument navDocument = new EpubNavDocument();
            try {
//                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser();
//                xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
//                xpp.setInput(new ByteArrayInputStream(response.getResponseBody()), "UTF-8");
                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                        new ByteArrayInputStream(response.getResponseBody()), "UTF-8");
                navDocument.load(xpp);
                containerView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        containerView.setTableOfContents(navDocument.getToc());
                    }
                });
            }catch(IOException e) {
                e.printStackTrace();
            }catch(XmlPullParserException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {

        }
    };

    
    /**
     * Empty constructor - this creates a blank unusable object - required for async loading
     */
    public ContainerController(Object context) {
         super(context);
    }

    public ContainerController(Object context, ContainerView view) {
        this(context);
        this.containerView = view;
    }

    public void onCreate(Hashtable arguments, Hashtable savedState) {
        String fileUri = (String)arguments.get(ARG_CONTAINERURI);
        UstadMobileSystemImpl.getInstance().mountContainer(
                new ContainerMountRequest(fileUri, true), 0,
                mountedCallbackHandler);
    }

    public UstadView getView() {
        return containerView;
    }
    
    public void setView(UstadView view) {
        this.containerView = (ContainerView)view;
        super.setView(view);
    }


    /**
     * Returns a Query String for the xAPI parameters for the container including 
     * the tincan actor, authorization, endpoint, and registration UUID
     * 
     * @return Query string as above
     */
    public String getXAPIQuery(String xapiEndpoint) {
        return "?actor=" +
            URLTextUtil.urlEncodeUTF8(UMTinCanUtil.makeActorFromActiveUser(getContext()).toString()) +
                //This file itself will be deleted
            //"&auth=" + URLTextUtil.urlEncodeUTF8(LoginController.encodeBasicAuth(username, password)) +
            "&endpoint=" + URLTextUtil.urlEncodeUTF8(xapiEndpoint) +
            "&registration=" + registrationUUID;
    }

    public String getXAPIQuery() {
        return getXAPIQuery(UMFileUtil.resolveLink(mountedUrl, "/xapi/"));
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
        if(containerView != null) {
            containerView.setPageTitle(pageTitle);
        }
    }
    
    
}
