package com.ustadmobile.test.sharedse;

import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.networkmanager.ResumableHttpDownload;
import com.ustadmobile.test.core.impl.ClassResourcesResponder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
    public void testResumableDownload() throws IOException, NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream resIn = getClass().getResourceAsStream("/com/ustadmobile/test/sharedse/thelittlechicks.epub");
        DigestInputStream din = new DigestInputStream(resIn, md);

        UMIOUtils.readFully(din, bout, 1024*16);
        din.close();
        long resLength = bout.toByteArray().length;
        byte[] resMd5 = md.digest();

        String httpDownloadUrl = httpRoot + "com/ustadmobile/test/sharedse/thelittlechicks.epub";
        File tmpDownloadFile = File.createTempFile("testresuambledownload", ".epub");
        ResumableHttpDownload resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
        boolean result = false;
        try {
            result = resumableDownload.download();
        }catch(IOException e) {
            System.err.println("Eexception on attempting download without interruption");
        }
        Assert.assertTrue("Download reported as OK", result);
        Assert.assertEquals("Download size matches expected length", resLength, tmpDownloadFile.length());


        bout = new ByteArrayOutputStream();
        md = MessageDigest.getInstance("MD5");
        din = new DigestInputStream(new FileInputStream(tmpDownloadFile), md);
        UMIOUtils.readFully(din, bout, 1024*16);
        din.close();
        bout.close();

        Assert.assertTrue("MD5 digest matches", Arrays.equals(resMd5, md.digest()));
        Assert.assertTrue("Deleted tmp download file for retry", tmpDownloadFile.delete());


        result = false;
        int retryLimit = 15;
        tmpDownloadFile = File.createTempFile("testresumabledownload-retry", ".epub");
        for(int i = 0; i < retryLimit && !result; i++) {
            httpDownloadUrl = httpRoot + "com/ustadmobile/test/sharedse/thelittlechicks.epub?cutoffafter=100000";
            resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
            try {
                result = resumableDownload.download();
            }catch(IOException e) {}
        }

        bout = new ByteArrayOutputStream();
        md = MessageDigest.getInstance("MD5");
        din = new DigestInputStream(new FileInputStream(tmpDownloadFile), md);
        UMIOUtils.readFully(din, bout, 1024*16);
        din.close();
        bout.close();

        Assert.assertTrue("MD5 digest matches on resumed file download", Arrays.equals(resMd5, md.digest()));
        Assert.assertEquals("Download size matches expected length", resLength, tmpDownloadFile.length());
        Assert.assertTrue("Download reported as OK", result);
        Assert.assertTrue("Deleted tmp download file for retry", tmpDownloadFile.delete());
    }

}
