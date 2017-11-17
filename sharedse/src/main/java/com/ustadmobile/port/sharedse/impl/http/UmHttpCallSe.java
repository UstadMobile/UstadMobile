package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.http.UmHttpCall;

import okhttp3.Call;

/**
 * Simple wrapper to map to OK HTTP library
 */

public class UmHttpCallSe extends UmHttpCall{

    private Call call;

    /**
     *
     * @param call the OK Http Call object
     */
    public UmHttpCallSe(Call call) {
        this.call = call;
    }

    @Override
    public void cancel() {
        call.cancel();
    }
}
