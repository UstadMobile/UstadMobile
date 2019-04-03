package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.ShowErrorUmCallback;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.ShowErrorUmHttpResponseCallback;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.tincan.TinCanXML;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.core.util.UMUUID;
import com.ustadmobile.core.view.UstadView;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.lib.db.entities.UmAccount;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

/**
 * Created by mike on 9/13/17.
 * <p>
 * Displays an XAPI Zip Package.
 * <p>
 * Pass EpubContentPresenter.ARG_CONTAINERURI when creating to provide the location of the xAPI
 * zip to open
 * <p>
 * Uses the Rustici launch method to find the URL to launch:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
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
            if (response.isSuccessful()) {
                try {
                    handleTinCanXmlLoaded(response.getResponseBody());
                } catch (IOException | XmlPullParserException | URISyntaxException e) {
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
        long containerUid = Long.parseLong((String) getArguments().get(UstadView.ARG_CONTAINER_UID));
        view.mountContainer(containerUid, new ZipMountedCallbackHandler());
    }

    private void handleTinCanXmlLoaded(byte[] tincanXmlBytes) throws IOException, XmlPullParserException, URISyntaxException {
        XmlPullParser xpp = UstadMobileSystemImpl.getInstance().newPullParser(
                new ByteArrayInputStream(tincanXmlBytes), "UTF-8");
        tinCanXml = TinCanXML.loadFromXML(xpp);
        launchHref = tinCanXml.getLaunchActivity().getLaunchUrl();
        String activityId = tinCanXml.getLaunchActivity().getId();
        launchUrl = UMFileUtil.joinPaths(mountedPath, launchHref);
        JSONObject actor = UMTinCanUtil.makeActorFromActiveUser(context);
        UmAccount account = UmAccountManager.getActiveAccount(context);

        StringBuilder launchUrlXapi = new StringBuilder(launchUrl);
        launchUrlXapi.append("?endpoint=");
        launchUrlXapi.append(URLEncoder.encode(UMFileUtil.resolveLink(mountedPath, "/xapi"), StandardCharsets.UTF_8.toString()));
        if (account != null && account.getAuth() != null && !account.getAuth().isEmpty()) {
            launchUrlXapi.append("&auth=");
            launchUrlXapi.append(URLEncoder.encode(account.getAuth(), StandardCharsets.UTF_8.toString()));
        }
        launchUrlXapi.append("&actor=");
        launchUrlXapi.append(URLEncoder.encode(actor.toString(), StandardCharsets.UTF_8.toString()));
        launchUrlXapi.append("&registration=");
        launchUrlXapi.append(URLEncoder.encode(registrationUUID, StandardCharsets.UTF_8.toString()));
        launchUrlXapi.append("&activity_id=");
        launchUrlXapi.append(activityId);

        view.runOnUiThread(() -> {
            view.setTitle(tinCanXml.getLaunchActivity().getName());
            view.loadUrl(launchUrlXapi.toString());
        });
    }

}
