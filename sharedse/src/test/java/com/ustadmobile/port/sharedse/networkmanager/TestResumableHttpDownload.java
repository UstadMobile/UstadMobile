package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
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

    private static final String TEST_RESOURCE_PATH =
            "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub";

    private class TestResourceInfo {

        private byte[] md5;

        private long length;

        private TestResourceInfo(byte[] md5, long length) {
            this.md5 = md5;
            this.length = length;
        }

        byte[] getMd5() {
            return md5;
        }

        long getLength() {
            return length;
        }
    }

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
    public static void stopHttpResourcesServer() {
        if(resourcesHttpd != null) {
            resourcesHttpd.stop();
            resourcesHttpd = null;
        }
    }

    private TestResourceInfo getResourceInfo(InputStream in) throws NoSuchAlgorithmException, IOException{
        DigestInputStream din = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            din = new DigestInputStream(in, md);
            UMIOUtils.readFully(din, bout, 1024 * 16);
            byte[] byteArr = bout.toByteArray();
            return new TestResourceInfo(md.digest(), byteArr.length);
        }catch(IOException ioe) {
            UMIOUtils.throwIfNotNull(ioe, IOException.class);
        }catch(NoSuchAlgorithmException nse) {
            UMIOUtils.throwIfNotNull(nse, NoSuchAlgorithmException.class);
        }finally{
            UMIOUtils.closeInputStream(din);
        }

        return null;
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void givenConnectionOk_whenDownloaded_md5ShouldMatch() throws IOException,
            NoSuchAlgorithmException{
        TestResourceInfo testResInfo = getResourceInfo(getClass().getResourceAsStream(
                TEST_RESOURCE_PATH));
        String httpDownloadUrl = UMFileUtil.joinPaths(httpRoot, TEST_RESOURCE_PATH);
        File tmpDownloadFile = File.createTempFile("testresuambledownload", ".epub");
        ResumableHttpDownload resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());

        boolean result = resumableDownload.download();

        Assert.assertTrue("Download reported as OK", result);
        Assert.assertEquals("Download size matches expected length", testResInfo.getLength(),
                tmpDownloadFile.length());

        TestResourceInfo downloadedFileInfo = getResourceInfo(new FileInputStream(tmpDownloadFile));

        Assert.assertTrue("MD5 digest matches", Arrays.equals(testResInfo.getMd5(),
                downloadedFileInfo.getMd5()));
        Assert.assertEquals("Downloaded file size equals resource size", testResInfo.getLength(),
                downloadedFileInfo.getLength());
        Assert.assertTrue("Temporary file deleted", tmpDownloadFile.delete());
    }

    @Test
    @SuppressWarnings({"EmptyCatchBlock", "ConstantConditions"})
    public void givenInterruptedConnection_whenDownloaded_md5ShouldMatch() throws IOException, NoSuchAlgorithmException{
        boolean result = false;
        int retryLimit = 15;
        File tmpDownloadFile = File.createTempFile("testresumabledownload-retry", ".epub");

        for(int i = 0; i < retryLimit && !result; i++) {
            String httpDownloadUrl = UMFileUtil.joinPaths(httpRoot,
                    TEST_RESOURCE_PATH + "?cutoffafter=100000");
            ResumableHttpDownload resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
            try {
                result = resumableDownload.download();
            }catch(IOException e) {

            }
        }

        TestResourceInfo resInfo = getResourceInfo(getClass().getResourceAsStream(TEST_RESOURCE_PATH));
        TestResourceInfo testDownloadInfo = getResourceInfo(new FileInputStream(tmpDownloadFile));

        Assert.assertTrue("MD5 digest matches on resumed file download",
                Arrays.equals(resInfo.getMd5(), testDownloadInfo.getMd5()));
        Assert.assertEquals("Download size matches expected length", resInfo.getLength(),
                tmpDownloadFile.length());
        Assert.assertTrue("Download reported as OK", result);
        Assert.assertTrue("Deleted tmp download file for retry", tmpDownloadFile.delete());
    }

    @Test
    public void givenRemoteFileNotExisting_whenDownloaded_shouldThrowException() throws IOException{
        String httpDownloadUrl = UMFileUtil.joinPaths(httpRoot, TEST_RESOURCE_PATH + "-wrongurl");
        File tmpDownloadFile = File.createTempFile("testresuambledownload-wrongurl", ".epub");
        ResumableHttpDownload resumableDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
        IOException ioe = null;

        try {
            resumableDownload.download();
        }catch(IOException e) {
            ioe = e;
        }

        Assert.assertNotNull("When download url does not exist, IOException is thrown",
                ioe);
        Assert.assertEquals("When download url does not exist, response code is 404",
                404, resumableDownload.getResponseCode());
    }

    @Test
    public void givenRemoteServerOffline_whenDownloaded_shouldThrowException() throws IOException{
        String httpDownloadUrl = UMFileUtil.joinPaths("http://localhost:42", TEST_RESOURCE_PATH);
        File tmpDownloadFile = File.createTempFile("testresumabledownload-offlineserver", ".epub");
        ResumableHttpDownload resumableHttpDownload = new ResumableHttpDownload(httpDownloadUrl,
                tmpDownloadFile.getAbsolutePath());
        IOException ioe = null;

        try {
            resumableHttpDownload.download();
        }catch(IOException e) {
            ioe = e;
        }

        Assert.assertNotNull("When server is offline, IOException is thrown", ioe);
        Assert.assertEquals("When server is offline, response code is 0", 0,
                resumableHttpDownload.getResponseCode());
    }
}
