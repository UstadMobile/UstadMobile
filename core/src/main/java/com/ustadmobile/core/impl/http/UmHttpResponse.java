package com.ustadmobile.core.impl.http;

import java.io.IOException;
import java.util.Hashtable;

/**
 * A wrapper that represents an HTTP response
 */

public abstract class UmHttpResponse {

    public UmHttpResponse() {
    }

    /**
     * Get a response header frmo the response
     *
     * @param headerName
     *
     * @return String with the header value
     */
    public abstract String getHeader(String headerName);

    /**
     * Get the response body as a byte array. This will read the entire response into the memory if
     * it was cached elsewhere.
     *
     * @return The response body as a byte array
     *
     * @throws IOException
     */
    public abstract byte[] getResponseBody() throws IOException;

    /**
     * Returns whether or not the respnose was successful, e.g. the response has a successful http
     * response code.
     *
     * @return true if the request was successful, false otherwise (e.g. 400+ status code)
     */
    public abstract boolean isSuccessful();


}
