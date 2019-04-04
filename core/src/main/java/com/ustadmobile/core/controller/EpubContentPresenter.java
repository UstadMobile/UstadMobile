/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2019 UstadMobile Inc.

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
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfItem;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.EpubContentView;
import com.ustadmobile.lib.util.UMUtil;
import com.ustadmobile.core.impl.UmCallback;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * Shows an EPUB with a table of contents, and page by page swipe navigation
 * 
 * @author mike
 */
public class EpubContentPresenter extends UstadBaseController<EpubContentView> {
    
    private EpubContentView epubContentView;

    private OcfDocument ocf;

    /**
     * Hardcoded fixed path to the container.xml file as per the open container
     * format spec : META-INF/container.xml
     */
    public static final String OCF_CONTAINER_PATH = "META-INF/container.xml";

    private String mountedUrl;

    private String opfBaseUrl;

    private String[] linearSpineUrls;

    /**
     * First HTTP callback: run this once the container has been mounted to an http directory
     *
     */
    private UmCallback<String> mountedCallbackHandler = new UmCallback<String>() {

        @Override
        public void onSuccess(String result) {
            mountedUrl = result;
            String containerUri = UMFileUtil.joinPaths(mountedUrl, OCF_CONTAINER_PATH);
            UstadMobileSystemImpl.getInstance().makeRequestAsync(
                    new UmHttpRequest(getContext(), containerUri), containerHttpCallbackHandler);
        }

        @Override
        public void onFailure(Throwable exception) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 500, "Exception mounting container");
            exception.printStackTrace();
        }
    };

    /**
     * Second HTTP callback: parses the container.xml file and finds the OPF
     */
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

    /**
     * Third HTTP callback: parses the OPF and sets up the view
     */
    private UmHttpResponseCallback opfHttpCallbackHandler = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            OpfDocument opf = new OpfDocument();
            try {
                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                        new ByteArrayInputStream(response.getResponseBody()));
                opf.loadFromOPF(xpp);
                final String[] linearSpineHrefsRelative = opf.getLinearSpineHREFs();

                opfBaseUrl = UMFileUtil.getParentFilename(UMFileUtil.joinPaths(
                        mountedUrl, ocf.getRootFiles().get(0).getFullPath()));

                linearSpineUrls = new String[linearSpineHrefsRelative.length];
                for(int i = 0; i < linearSpineHrefsRelative.length; i++) {
                    linearSpineUrls[i] = UMFileUtil.joinPaths(opfBaseUrl,
                            linearSpineHrefsRelative[i]);
                }

                final OpfItem opfCoverImageItem = opf.getCoverImage(null);
                final String authorNames = opf.getNumCreators() > 0 ?
                        UMUtil.joinStrings(opf.getCreators(), ", ") : null;

                epubContentView.runOnUiThread(() -> {
                    epubContentView.setContainerTitle(opf.getTitle());
                    epubContentView.setSpineUrls(linearSpineUrls);
                    if(opfCoverImageItem != null) {
                        epubContentView.setCoverImage(UMFileUtil.resolveLink(opfBaseUrl,
                                opfCoverImageItem.href));
                    }

                    if(authorNames != null) {
                        epubContentView.setAuthorName(authorNames);
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
            exception.printStackTrace();
        }
    };

    private UmHttpResponseCallback navCallbackHandler = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            final EpubNavDocument navDocument = new EpubNavDocument();
            try {
                XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                        new ByteArrayInputStream(response.getResponseBody()), "UTF-8");
                xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                navDocument.load(xpp);
                epubContentView.runOnUiThread(() ->
                        epubContentView.setTableOfContents(navDocument.getToc()));
                view.runOnUiThread(() -> view.setProgressBarVisible(false));
            }catch(IOException e) {
                e.printStackTrace();
            }catch(XmlPullParserException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            exception.printStackTrace();
        }
    };

    public EpubContentPresenter(Object context, Hashtable args, EpubContentView view) {
        super(context, args, view);
        this.epubContentView = view;
    }

    public void onCreate(Hashtable savedState) {
        long containerUid = Long.parseLong(getArguments().get(EpubContentView.ARG_CONTAINER_UID)
                .toString());
        view.setProgressBarProgress(-1);
        view.setProgressBarVisible(true);
        view.mountContainer(containerUid, mountedCallbackHandler);
    }

    public void handlePageTitleUpdated(String pageTitle) {
        if(epubContentView != null) {
            epubContentView.setPageTitle(pageTitle);
        }
    }

    public void handleClickNavItem(EpubNavItem navItem) {
        if(opfBaseUrl != null && linearSpineUrls != null) {
            String navItemUrl = UMFileUtil.resolveLink(opfBaseUrl, navItem.getHref());
            int hrefIndex = Arrays.asList(linearSpineUrls).indexOf(navItemUrl);
            if(hrefIndex != -1) {
                epubContentView.goToLinearSpinePosition(hrefIndex);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mountedUrl != null){
            view.unmountContainer(mountedUrl);
        }
    }
}
