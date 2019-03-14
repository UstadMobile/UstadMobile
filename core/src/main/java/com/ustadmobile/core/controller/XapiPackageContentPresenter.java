package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.XapiPackageContentView;

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
 * Pass EpubContentPresenter.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 *
 * Uses the Rustici launch method to find the URL to launch:
 *  https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 */
public class XapiPackageContentPresenter extends UstadBaseController<XapiPackageContentView> {

    private String mountedPath;

    private TinCanXML tinCanXml;

    private String launchHref;

    private String launchUrl;

    private String registrationUUID;

    private UmCallback zipMountedCallbackHandler = new UmCallback() {
        @Override
        public void onSuccess(Object result) {
            mountedPath = (String)result;
            UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(
                    getContext(),
                    UMFileUtil.joinPaths(mountedPath, "tincan.xml")),
                    tincanXmlResponseCallback);
        }

        @Override
        public void onFailure(Throwable exception) {
            view.showErrorNotification(UstadMobileSystemImpl.getInstance().getString(
                    MessageID.error_opening_file, getContext()));
        }
    };


    private UmHttpResponseCallback tincanXmlResponseCallback = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            if(response.isSuccessful()) {
                try {
                    handleTinCanXmlLoaded(response.getResponseBody());
                }catch(IOException|XmlPullParserException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 75, null, e);
                    onFailure(call, new IOException(e));
                }
            }else {
                onFailure(call, new IOException("TinCan.xml request not successful"));
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {
            view.showErrorNotification(UstadMobileSystemImpl.getInstance().getString(
                    MessageID.error_opening_file, getContext()));
        }
    };


    public XapiPackageContentPresenter(Object context, Hashtable args, XapiPackageContentView view) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        registrationUUID = UMUUID.randomUUID().toString();
        long containerUid = Long.parseLong((String)getArguments().get(UstadView.ARG_CONTAINER_UID));
        view.mountContainer(containerUid, zipMountedCallbackHandler);
    }

    private void handleTinCanXmlLoaded(byte[] tincanXmlBytes) throws IOException, XmlPullParserException{
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                new ByteArrayInputStream(tincanXmlBytes), "UTF-8");
        tinCanXml = TinCanXML.loadFromXML(xpp);
        launchHref = tinCanXml.getLaunchActivity().getLaunchUrl();
        launchUrl = UMFileUtil.joinPaths(mountedPath, launchHref);
        view.runOnUiThread(() -> {
            view.setTitle(tinCanXml.getLaunchActivity().getName());
            view.loadUrl(launchUrl);
        });
    }

}
