package com.ustadmobile.core.impl.http;

import java.io.IOException;

/**
 * Simple wrapper for asynchronous http
 */
public interface UmHttpResponseCallback {

    /**
     * Called when the http call was completed. This will be called if a failure http status
     * code is provided (e.g. 400+). Use response.isSuccessful to check
     *
     * @param call The call that was actually completed
     * @param response The http response
     */
    void onComplete(UmHttpCall call, UmHttpResponse response);

    /**
     * Called when the http call failed (e.g. if the net is disconnected, etc).
     *  @param call The call that has failed
     * @param exception exception that occurred
     */
    void onFailure(UmHttpCall call, IOException exception);

}
