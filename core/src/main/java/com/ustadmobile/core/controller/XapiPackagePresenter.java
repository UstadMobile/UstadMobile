package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.util.URLTextUtil;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.util.UMUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by mike on 9/13/17.
 *
 * Displays an XAPI Zip Package.
 *
 * Pass ContainerController.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 *  https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
public class XapiPackagePresenter extends UstadBaseController {

    private Hashtable args;

    private XapiPackageView view;

    String mountedPath;

    private TinCanXML tinCanXml;

    private String launchHref;

    private String launchUrl;

    private String registrationUUID;

    private boolean loadCompleted;

    private UmCallback loadListener;

    private UmCallback zipMountedCallbackHandler = new UmCallback() {
        @Override
        public void onSuccess(Object result) {
            mountedPath = (String)result;
            UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(
                    getContext(),
                    UMFileUtil.joinPaths(new String[]{mountedPath, "tincan.xml"})),
                    tincanXmlResponseCallback);
        }

        @Override
        public void onFailure(Throwable exception) {
            //TODO: implement this.
        }
    };


    private UmHttpResponseCallback tincanXmlResponseCallback = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            if(response.isSuccessful()) {
                try {
                    handleTinCanXmlLoaded(response.getResponseBody());
                }catch(IOException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 75, null, e);
                }
            }else {
                //go to the next sub directory
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {

        }


        private void tryNextDir() {

        }
    };


    public XapiPackagePresenter(Object context, XapiPackageView view) {
        super(context);
        this.view = view;
    }

    public void onCreate(Hashtable args) {
        this.args = args;
        registrationUUID = UMUUID.randomUUID().toString();
        view.mountZip((String)args.get(ContainerController.ARG_CONTAINERURI),
                zipMountedCallbackHandler);
    }

    private void handleTinCanXmlLoaded(byte[] tincanXmlBytes) {
        try {
            XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                    new ByteArrayInputStream(tincanXmlBytes), "UTF-8");
            tinCanXml = TinCanXML.loadFromXML(xpp);
            launchHref = tinCanXml.getLaunchActivity().getLaunchUrl();
            launchUrl = UMFileUtil.joinPaths(new String[]{mountedPath, launchHref});
            launchUrl += getXAPIQuery();
            view.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setTitle(tinCanXml.getLaunchActivity().getName());
                    view.loadUrl(launchUrl);
                }
            });
            loadCompleted = true;
            if(loadListener != null)
                loadListener.onSuccess(null);
        }catch(XmlPullParserException xe) {

        }catch(IOException e) {

        }

    }

    /**
     * Returns a Query String for the xAPI parameters for the container including
     * the tincan actor, authorization, endpoint, and registration UUID
     *
     * @return Query string as above
     */
    public String getXAPIQuery() {
        UmAccount activeAccount = UmAccountManager.getActiveAccount(getContext());
        String username = activeAccount.getUsername();
        String password = activeAccount.getAuth();
        String xapiEndpoint = UMFileUtil.resolveLink(mountedPath, "/xapi/");
        String activityArgs = tinCanXml != null && tinCanXml.getLaunchActivity() != null ?
                "&activity_id=" + URLTextUtil.urlEncodeUTF8(tinCanXml.getLaunchActivity().getId()) : "";

        return "?actor=" +
                URLTextUtil.urlEncodeUTF8(UMTinCanUtil.makeActorFromActiveUser(getContext()).toString()) +
                "&auth=" + URLTextUtil.urlEncodeUTF8(UMUtil.encodeBasicAuth(username, password)) +
                "&endpoint=" + URLTextUtil.urlEncodeUTF8(xapiEndpoint) +
                "&registration=" + registrationUUID +
                activityArgs;
    }

    @Override
    public void setUIStrings() {

    }

    public UmCallback getOnLoadListener() {
        return loadListener;
    }

    public void setOnLoadListener(UmCallback loadListener) {
        this.loadListener = loadListener;
    }

    public boolean isLoadCompleted() {
        return loadCompleted;
    }

    public TinCanXML getTinCanXml() {
        return tinCanXml;
    }
}
