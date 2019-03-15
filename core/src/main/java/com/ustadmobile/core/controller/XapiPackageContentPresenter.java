package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.ShowErrorUmCallback;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.ShowErrorUmHttpResponseCallback;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
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

    private class ZipMountedCallbackHandler extends ShowErrorUmCallback<String> {

        public ZipMountedCallbackHandler() {
            super(view, MessageID.error_opening_file);
        }

        @Override
        public void onSuccess(String result) {
            mountedPath = result;
            UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(
                            getContext(),
                            UMFileUtil.joinPaths(mountedPath, "tincan.xml")),
                    new TinCanResponseCallback());
        }
    }

    private class TinCanResponseCallback extends ShowErrorUmHttpResponseCallback {

        private TinCanResponseCallback() {
            super(view, MessageID.error_opening_file);
        }

        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            super.onComplete(call, response);
            if(response.isSuccessful()) {
                try {
                    handleTinCanXmlLoaded(response.getResponseBody());
                } catch (IOException | XmlPullParserException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 75, null, e);
                    onFailure(call, new IOException(e));
                }
            }
        }
    }

    public XapiPackageContentPresenter(Object context, Hashtable args, XapiPackageContentView view) {
        super(context, args, view);
    }

    public void onCreate(Hashtable savedState) {
        registrationUUID = UMUUID.randomUUID().toString();
        long containerUid = Long.parseLong((String)getArguments().get(UstadView.ARG_CONTAINER_UID));
        view.mountContainer(containerUid, new ZipMountedCallbackHandler());
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
