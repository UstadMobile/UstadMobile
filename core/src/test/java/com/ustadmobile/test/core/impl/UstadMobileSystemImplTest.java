package com.ustadmobile.test.core.impl;

import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.TinCanQueueListener;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.core.impl.se.UMTestLogger;

import org.json.JSONObject;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by mike on 4/25/17.
 */

public class UstadMobileSystemImplTest extends UstadMobileSystemImplSE {

    /**
     * In testing cache dirs are simpyl temporary directories
     */
    private HashMap<Integer, File> cacheDirs;

    /**
     * System base dir will be a temporary directory
     */
    private File testSystemBaseDir;

    private UMTestLogger testLogger;


    public UstadMobileSystemImplTest() {
        cacheDirs = new HashMap<>();
        testSystemBaseDir = makeTempDir("umTestSystemDir", "");
        testLogger = new UMTestLogger();
    }

    protected File makeTempDir(String prefix, String suffix) {
        File tmpDir = null;
        try {
            tmpDir = File.createTempFile(prefix, suffix);
            tmpDir.delete();
            tmpDir.mkdir();
            tmpDir.deleteOnExit();
        }catch(IOException e) {
            System.err.println("Exception with makeTempDir");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return tmpDir;
    }

    @Override
    public void go(String viewName, Hashtable args, Object context) {

    }

    @Override
    public String getCacheDir(int mode, Object context) {
        File tmpDir = cacheDirs.get(mode);
        if(tmpDir == null) {
            tmpDir = makeTempDir("umcache-" + mode, "");
            cacheDirs.put(mode, tmpDir);
        }

        return tmpDir.getAbsolutePath();
    }


    @Override
    public XmlPullParser newPullParser() throws XmlPullParserException {
        return new KXmlParser();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return url.openConnection();
    }

    @Override
    protected String getSystemBaseDir() {
        return testSystemBaseDir.getAbsolutePath();
    }

    @Override
    public boolean loadActiveUserInfo(Object context) {
        return false;
    }

    @Override
    public String getImplementationName() {
        return "Test";
    }

    @Override
    public String getSharedContentDir() {
        return null;
    }

    @Override
    public String getUserContentDirectory(String username) {
        return null;
    }

    @Override
    public Hashtable getSystemInfo() {
        return null;
    }

    @Override
    public String queueFileDownload(String url, String fileURI, Hashtable headers, Object context) {
        return null;
    }

    @Override
    public int[] getFileDownloadStatus(String downloadID, Object context) {
        return new int[0];
    }

    @Override
    public void registerDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {

    }

    @Override
    public void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver, Object context) {

    }

    @Override
    public String getActiveUser(Object context) {
        return null;
    }

    @Override
    public void setActiveUserAuth(String password, Object context) {

    }

    @Override
    public String getActiveUserAuth(Object context) {
        return null;
    }

    @Override
    public void setUserPref(String key, String value, Object context) {

    }

    @Override
    public String getUserPref(String key, Object context) {
        return null;
    }

    @Override
    public String[] getUserPrefKeyList(Object context) {
        return new String[0];
    }

    @Override
    public void saveUserPrefs(Object context) {

    }

    @Override
    public String getAppPref(String key, Object context) {
        return null;
    }

    @Override
    public String[] getAppPrefKeyList(Object context) {
        return new String[0];
    }

    @Override
    public void setAppPref(String key, String value, Object context) {

    }

    @Override
    public AppView getAppView(Object context) {
        return null;
    }

    @Override
    public UMLog getLogger() {
        return testLogger;
    }

    @Override
    public String getUMProfileName() {
        return null;
    }

    @Override
    public String getMimeTypeFromExtension(String extension) {
        return null;
    }

    @Override
    public String getExtensionFromMimeType(String mimeType) {
        return null;
    }

    @Override
    public void getResumableRegistrations(String activityId, Object context, TinCanResultListener listener) {

    }

    @Override
    public long getBuildTime() {
        return 0;
    }

    @Override
    public String getVersion(Object context) {
        return null;
    }
}
