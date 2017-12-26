package com.ustadmobile.test.sharedse.impl;

import com.ustadmobile.core.catalog.contenttype.EPUBTypePlugin;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.impl.TestContext;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

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
    public NetworkManager getNetworkManager() {
        return null;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {

    }
    
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
    protected String getSystemBaseDir(Object context) {
        TestContext ctx = (TestContext)context;
        return new File(testSystemBaseDir, ctx.getContextName()).getAbsolutePath();
    }

    @Override
    public String getString(int messageCode, Object context) {
        return "";
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
        return testSystemBaseDir.getAbsolutePath();
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
    public int[] getFileDownloadStatus(String downloadID, Object context) {
        return new int[0];
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
        return ((TestContext)context).getAppProps().getProperty(key);
    }

    @Override
    public String[] getAppPrefKeyList(Object context) {
        Properties appProps = ((TestContext)context).getAppProps();
        Set keySet = appProps.keySet();
        String[] propNames = new String[keySet.size()];
        keySet.toArray(propNames);

         return propNames;
    }

    @Override
    public void setAppPref(String key, String value, Object context) {
        TestContext tContext = (TestContext)context;
        if(value != null) {
            tContext.getAppProps().setProperty(key, value);
        }else {
            tContext.getAppProps().remove(key);
        }

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
    public void getResumableRegistrations(String activityId, Object context, TinCanResultListener listener) {

    }

    @Override
    public String getVersion(Object context) {
        return null;
    }

    @Override
    public Class[] getSupportedContentTypePlugins() {
        return new Class[] {EPUBTypePlugin.class};
    }

    @Override
    public String getManifestPreference(String key, Object context) {
        //TODO: Implement this
        return null;
    }

    @Override
    public String getUserDetail(String username, int field, Object dbContext) {
        return null;
    }

    @Override
    public void mountContainer(ContainerMountRequest request, int id, UmCallback callback) {
        //do nothing at the moment
    }
}
