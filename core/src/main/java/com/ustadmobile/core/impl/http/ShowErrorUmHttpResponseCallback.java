package com.ustadmobile.core.impl.http;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ViewWithErrorNotifier;

import java.io.IOException;

/**
 * Utility callback that will use the views method to show an error when an HTTP request fails or
 * the response isSuccessful is false.
 */
public abstract class ShowErrorUmHttpResponseCallback implements UmHttpResponseCallback {

    private ViewWithErrorNotifier view;

    private int errorMessageId = -1;

    public ShowErrorUmHttpResponseCallback(ViewWithErrorNotifier view, int errorMessageId) {
        this.view = view;
        this.errorMessageId = errorMessageId;
    }

    @Override
    public void onComplete(UmHttpCall call, UmHttpResponse response) {
        if(!response.isSuccessful()) {
            onFailure(call, new IOException());
        }
    }

    @Override
    public void onFailure(UmHttpCall call, IOException exception) {
        view.showErrorNotification(UstadMobileSystemImpl.getInstance().getString(errorMessageId,
                view.getContext()));
    }
}
