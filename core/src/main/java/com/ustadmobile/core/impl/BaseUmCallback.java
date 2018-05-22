package com.ustadmobile.core.impl;

/**
 * Created by mike on 1/29/18.
 */

public abstract class BaseUmCallback<T> implements UmCallback<T> {

    @Override
    public void onFailure(Throwable exception) {
        exception.printStackTrace();
    }
}
