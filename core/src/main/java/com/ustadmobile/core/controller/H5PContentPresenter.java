package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.H5PContentView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Created by mike on 2/15/18.
 */

public class H5PContentPresenter extends UstadBaseController {

    private H5PContentView h5PContentView;

    private String h5pFileUri;

    private String h5pDistMountUrl;

    private String h5pFileMountUrl;


    private UmCallback<String> mH5PDistMountedCallback = new UmCallback<String>() {
        @Override
        public void onSuccess(String h5pUrl) {
            h5pDistMountUrl = h5pUrl;
            h5PContentView.mountH5PFile(h5pFileUri, h5PFileMountedCallback);
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmCallback<String> h5PFileMountedCallback = new UmCallback<String>() {
        @Override
        public void onSuccess(String result) {
            h5pFileMountUrl = result;
            UstadMobileSystemImpl.getInstance().getAsset(getContext(),
                    "/com/ustadmobile/core/h5p/contentframe.html", contentFrameLoadedCallback);
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmCallback<InputStream> contentFrameLoadedCallback = new UmCallback<InputStream>() {

        @Override
        public void onSuccess(InputStream result) {
            try {
                String htmlStr = UMIOUtils.readStreamToString(result);
                htmlStr = htmlStr.replace("$DISTPATH", h5pDistMountUrl);
                final String subHtmlStr = htmlStr.replace("$CONTENTPATH", h5pFileMountUrl);
                h5PContentView.runOnUiThread(() -> h5PContentView.setContentHtml(h5pFileMountUrl,
                        subHtmlStr));
                UmHttpRequest h5PJsonRequest = new UmHttpRequest(getContext(),
                        UMFileUtil.joinPaths(h5pFileMountUrl, "h5p.json"));
                UstadMobileSystemImpl.getInstance().makeRequestAsync(h5PJsonRequest, h5pResponseCallback);
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Throwable exception) {

        }
    };

    private UmHttpResponseCallback h5pResponseCallback = new UmHttpResponseCallback() {
        @Override
        public void onComplete(UmHttpCall call, UmHttpResponse response) {
            try {
                if(response.isSuccessful()) {
                    String jsonStr = UMIOUtils.readStreamToString(response.getResponseAsStream());
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    h5PContentView.runOnUiThread( () -> h5PContentView.setTitle(
                            jsonObj.getString("title")));
                }
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(UmHttpCall call, IOException exception) {

        }
    };





    public H5PContentPresenter(Object context, H5PContentView h5PContentView) {
        super(context);
        this.h5PContentView = h5PContentView;
    }

    public void onCreate(Hashtable args) {
        this.h5pFileUri = (String)args.get(ContainerController.ARG_CONTAINERURI);
        h5PContentView.mountH5PDist(mH5PDistMountedCallback);
    }

    @Override
    public void setUIStrings() {

    }
}
