package com.ustadmobile.core.impl;

/**
 * Used to override the result of a callback (e.g. when doing an insert, and syncable primary keys
 * have already been generated.
 *
 * @param <T> Callback result type
 */
public class UmCallbackResultOverrider<T> implements UmCallback<T>{

    private T resultOverride;

    private UmCallback<T> callback;

    public UmCallbackResultOverrider(UmCallback<T> callback, T resultOverride) {
        this.callback = callback;
        this.resultOverride = resultOverride;
    }

    @Override
    public void onSuccess(T result) {
        if(callback != null)
            callback.onSuccess(resultOverride);
    }

    @Override
    public void onFailure(Throwable exception) {
        if(callback != null)
            callback.onFailure(exception);
    }
}
