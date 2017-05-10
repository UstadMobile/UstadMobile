package com.ustadmobile.test.sharedse.impl;

import com.ustadmobile.test.core.impl.UstadMobileTestUtil;

import java.io.IOException;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 4/27/17.
 */

public abstract class UstadMobileTestUtilSE extends UstadMobileTestUtil {

    private RouterNanoHTTPD httpd;

    public static final String HTTP_RESOURCES_DIR = "/res/";

    public UstadMobileTestUtilSE() {
        httpd = new RouterNanoHTTPD(0);

    }


    @Override
    public void startServer() throws IOException {
        httpd.start();
        httpd.addRoute(HTTP_RESOURCES_DIR + "(.)+", ClassResourcesResponder.class, HTTP_RESOURCES_DIR);
    }


    @Override
    public String getHttpEndpoint() {
        return "http://localhost:" + httpd.getListeningPort() + HTTP_RESOURCES_DIR;
    }
}
