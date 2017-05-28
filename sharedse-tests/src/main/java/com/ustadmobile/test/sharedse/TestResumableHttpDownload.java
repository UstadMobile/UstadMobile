package com.ustadmobile.test.sharedse;

import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 5/28/17.
 */

public class TestResumableHttpDownload {

    private static RouterNanoHTTPD resourcesHttpd;

    private static String httpRoot;

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        if(resourcesHttpd == null) {
            resourcesHttpd = new RouterNanoHTTPD(0);
            resourcesHttpd.addRoute("/res/(.*)", ClassResourcesResponder.class, "/res/");
            resourcesHttpd.start();
            httpRoot = "http://localhost:" + resourcesHttpd.getListeningPort() + "/res/";
        }
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    /**
     *
     */
    @Test
    public void testResumableDownload() throws IOException{
        String httpDownloadUrl = httpRoot + "com/ustadmobile/test/sharedse/thelittlechicks.epub";
        File tmpDownloadFile = File.createTempFile("testresuambledownload", ".epub");
        ResumableHttpDownload resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
        boolean result = resumableDownload.download();
        Assert.assertTrue("Download reported as OK", result);
    }

}
