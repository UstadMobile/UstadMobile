package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.WebChunkView;

import java.util.Hashtable;

import static com.ustadmobile.core.view.WebChunkView.ARG_CHUNK_PATH;

public class WebChunkPresenter extends UstadBaseController<WebChunkView> {

    public WebChunkPresenter(Object context, Hashtable arguments, WebChunkView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
        view.mountChunk((String) getArguments().get(ARG_CHUNK_PATH), new UmCallback<String>() {
            @Override
            public void onSuccess(String firstUrl) {
                view.loadUrl(firstUrl);
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }

    @Override
    public void setUIStrings() {

    }
}
