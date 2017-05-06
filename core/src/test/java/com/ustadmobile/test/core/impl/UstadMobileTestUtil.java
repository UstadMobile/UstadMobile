package com.ustadmobile.test.core.impl;

import java.io.IOException;

/**
 * TestUtil is designed to abstract away the differences between conducting testing on "smart"
 * devices where we can run NanoHTTPD and J2ME where we need to use an external server.
 */

public abstract class UstadMobileTestUtil {

    private static UstadMobileTestUtil singleton;

    public static UstadMobileTestUtil getInstance() {
        if(singleton == null) {
            singleton = UstadMobileTestUtilFactory.makeTestUtil();
        }

        return singleton;
    }

    /**
     * Starts the test HTTP server that should provide access to the required test resources
     *
     * @throws IOException
     */
    public abstract void startServer() throws IOException;

    /**
     * Provides the endpoint where resources can be found e.g.
     * http://localhost:4047/res/
     *
     * @return
     */
    public abstract String getHttpEndpoint();


}
