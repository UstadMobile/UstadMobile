package com.ustadmobile.test.core.scorm;

import com.ustadmobile.test.core.ResourcesHttpdTestServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by mike on 1/6/18.
 */

public class TestScormManifest {


    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }

    public void testParseScormManifest() {

    }


}
