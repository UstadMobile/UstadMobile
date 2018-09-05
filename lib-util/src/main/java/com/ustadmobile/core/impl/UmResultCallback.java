package com.ustadmobile.core.impl;

/**
 * Simple callback definition that has only one method with a single type parameter... and is thus
 * suitable for use with Lambdas
 */
public interface UmResultCallback<T> {

    void onDone(T result);
}
