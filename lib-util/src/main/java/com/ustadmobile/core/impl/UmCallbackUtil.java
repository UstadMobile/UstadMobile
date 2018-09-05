package com.ustadmobile.core.impl;

public class UmCallbackUtil {

    public static <T> void onSuccessIfNotNull(UmCallback<T> callback, T result) {
        if(callback != null)
            callback.onSuccess(result);
    }

    public static <T> void onFailIfNotNull(UmCallback<T> callback, Throwable exception) {
        if(callback != null)
            callback.onFailure(exception);
    }
}
