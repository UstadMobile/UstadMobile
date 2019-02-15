package com.ustadmobile.port.javase.impl;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

/**
 * Created by mike on 10/17/17.
 */

public class UstadMobileSystemImplJavaSe extends UstadMobileSystemImplSE {

    private UMLogJavaSe logJavaSe;

    private File systemDir;

    public UstadMobileSystemImplJavaSe() {
        logJavaSe = new UMLogJavaSe();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return url.openConnection();
    }

    @Override
    protected String getSystemBaseDir(Object context) {
        if(systemDir == null) {
            systemDir = makeTempDir("tmp-test-javase", "");
        }

        return systemDir.getAbsolutePath();
    }

    @Override
    public void go(String viewName, Hashtable args, Object context, int flags) {

    }

    @Override
    public String getString(int messageCode, Object context) {
        return ""+messageCode;
    }

    @Override
    public NetworkManagerBle getNetworkManagerBle() {
        return null;
    }

    @Override
    public String getSharedContentDir(Object context) {
        return null;
    }

    @Override
    public String getUserContentDirectory(Object context, String username) {
        return null;
    }

    @Override
    public String getAppPref(String key, Object context) {
        return null;
    }

    @Override
    public void setAppPref(String key, String value, Object context) {

    }

    @Override
    public UMLog getLogger() {
        return logJavaSe;
    }

    @Override
    public String getVersion(Object context) {
        return null;
    }

    @Override
    public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {

    }

    @Override
    public String getManifestPreference(String key, Object context) {
        return null;
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
    public void getAsset(Object context, String path, UmCallback<InputStream> callback) {
        if(!path.startsWith("/"))
            path = '/' + path;

        try {
            callback.onSuccess(getClass().getResourceAsStream(path));
        }catch(Exception e) {
            callback.onFailure(e);
        }
    }

    @Override
    public InputStream getAssetSync(Object context, String path) throws IOException {
        if(!path.startsWith("/"))
            path = '/' + path;

        return getClass().getResourceAsStream(path);
    }

    @Override
    public long getBuildTimestamp(Object context) {
        return 0;//not implemented
    }
}
