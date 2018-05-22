package com.ustadmobile.core.impl.http;

import java.io.IOException;

/**
 * Created by mike on 12/26/17.
 */

public class UmHttpException extends IOException {

    private UmHttpResponse response;

    private Exception rootCause;

    public UmHttpException(UmHttpResponse response) {
        this.response = response;
    }

    public UmHttpException(Exception rootCause) {
        this.rootCause = rootCause;
    }

    public int getStatus() {
        if(response != null)
            return response.getStatus();
        else
            return -1;
    }

    public Exception getRootCause() {
        return rootCause;
    }

}
