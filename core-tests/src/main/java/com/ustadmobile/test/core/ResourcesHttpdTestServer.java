package com.ustadmobile.test.core;

import com.ustadmobile.test.core.impl.ClassResourcesResponder;

import java.io.IOException;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 8/3/17.
 */

public class ResourcesHttpdTestServer {

    private static RouterNanoHTTPD resourcesHttpd;

    private static String httpRoot;

    public static void startServer() throws IOException {
        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
            httpRoot = "http://localhost:" + resourcesHttpd.getListeningPort() + "/res/";
        }
    }

    public static void stopServer() throws IOException{
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    public static String getHttpRoot() {
        return httpRoot;
    }

}
