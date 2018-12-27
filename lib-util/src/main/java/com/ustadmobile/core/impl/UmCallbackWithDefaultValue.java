package com.ustadmobile.core.impl;

public class UmCallbackWithDefaultValue<T> implements UmCallback<T>{

    private T defaultVal;

    private UmCallback<T> callback;

    public UmCallbackWithDefaultValue(T defaultVal, UmCallback callback) {
        this.defaultVal = defaultVal;
        this.callback = callback;
    }

    @Override
    public void onSuccess(T result) {
        if(result != null)
            UmCallbackUtil.onSuccessIfNotNull(callback, result);
        else
            UmCallbackUtil.onSuccessIfNotNull(callback, defaultVal);
    }

    @Override
    public void onFailure(Throwable exception) {
        UmCallbackUtil.onFailIfNotNull(callback, exception);
    }
}
