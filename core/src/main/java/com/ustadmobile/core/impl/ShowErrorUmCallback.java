package com.ustadmobile.core.impl;

import com.ustadmobile.core.view.ViewWithErrorNotifier;

/**
 * Utility callback that will automatically call the showErrorNotification on a view if the
 * callback's onFailure method is called
 *
 * @param <T> Callback type
 */
public abstract class ShowErrorUmCallback<T> implements UmCallback<T> {

    private ViewWithErrorNotifier view;

    private int errorMessage;

    public ShowErrorUmCallback(ViewWithErrorNotifier view, int errorMessage) {
        this.view = view;
        this.errorMessage = errorMessage;
    }

    @Override
    public void onFailure(Throwable exception) {
        view.showErrorNotification(UstadMobileSystemImpl.getInstance().getString(
                errorMessage, view.getContext()));
    }
}
