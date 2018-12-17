/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.fs.db.repository.OpdsAtomFeedRepositoryImpl;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.lib.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.impl.http.UmHttpCallSe;
import com.ustadmobile.port.sharedse.impl.http.UmHttpResponseSe;
import com.ustadmobile.port.sharedse.impl.zip.ZipFileHandleSharedSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;

import com.ustadmobile.core.listener.ActiveSyncListener;
import com.ustadmobile.core.listener.ActiveUserListener;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author mike
 */
public abstract class UstadMobileSystemImplSE extends UstadMobileSystemImpl implements UstadMobileSystemImplFs {

    private XmlPullParserFactory xmlPullParserFactory;

    Vector activeUserListener = new Vector();
    Vector activeSyncListener = new Vector();
    //ActiveSyncListener activeSyncListener;

    private HttpCache httpCache;

    private final OkHttpClient client = new OkHttpClient();

    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";

    private Properties appConfig;

    private OpdsAtomFeedRepository atomFeedRepository;

    /**
     * Convenience method to return a casted instance of UstadMobileSystemImplSharedSE
     *
     * @return Casted UstadMobileSystemImplSharedSE
     */
    public static UstadMobileSystemImplSE getInstanceSE() {
        return (UstadMobileSystemImplSE)UstadMobileSystemImpl.getInstance();
    }

    @Override
    public void init(Object context) {
        super.init(context);

        if(httpCache == null)
            httpCache = new HttpCache(getCacheDir(CatalogPresenter.SHARED_RESOURCE, context));
    }

    /**
     * Open the given connection and return the HttpURLConnection object using a proxy if required
     *
     * @param url
     *
     * @return
     */
    public abstract URLConnection openConnection(URL url) throws IOException;

    @Override
    public boolean isJavascriptSupported() {
        return true;
    }

    @Override
    public boolean isHttpsSupported() {
        return true;
    }

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    protected abstract String getSystemBaseDir(Object context);


    @Override
    public String getCacheDir(int mode, Object context) {
        String systemBaseDir = getSystemBaseDir(context);
        if(mode == CatalogPresenter.SHARED_RESOURCE) {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, UstadMobileConstants.CACHEDIR});
        }else {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-" + getActiveUser(context),
                    UstadMobileConstants.CACHEDIR});
        }
    }

    @Override
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        List<UMStorageDir> dirList = new ArrayList<>();
        String systemBaseDir = getSystemBaseDir(context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final String contentDirName = getContentDirName(context);

        if((mode & CatalogPresenter.SHARED_RESOURCE) == CatalogPresenter.SHARED_RESOURCE) {
            dirList.add(new UMStorageDir(systemBaseDir, getString(MessageID.device, context),
                    false, true, false));

            //Find external directories
            String[] externalDirs = findRemovableStorage();
            for(String extDir : externalDirs) {
                dirList.add(new UMStorageDir(UMFileUtil.joinPaths(new String[]{extDir,
                        contentDirName}),
                        getString(MessageID.memory_card, context),
                        true, true, false, false));
            }
        }

        if(impl.getActiveUser(context) != null
                && ((mode & CatalogPresenter.USER_RESOURCE) == CatalogPresenter.USER_RESOURCE)) {
            String userBase = UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-"
                    + getActiveUser(context)});
            dirList.add(new UMStorageDir(userBase, getString(MessageID.device, context),
                    false, true, true));
        }




        UMStorageDir[] retVal = new UMStorageDir[dirList.size()];
        dirList.toArray(retVal);
        return retVal;
    }

    /**
     * Provides a list of paths to removable stoage (e.g. sd card) directories
     *
     * @return
     */
    public String[] findRemovableStorage() {
        return new String[0];
    }

    /**
     * Will return language_COUNTRY e.g. en_US
     *
     * @return
     */
    @Override
    public String getSystemLocale(Object context) {
        return Locale.getDefault().toString();
    }


    @Override
    public long fileLastModified(String fileURI) {
        return new File(fileURI).lastModified();
    }

    @Override
    public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileOutputStream(fileURI, (flags & FILE_APPEND) == FILE_APPEND);
    }

    @Override
    public InputStream openFileInputStream(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileInputStream(fileURI);
    }


    @Override
    public boolean fileExists(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new File(fileURI).exists();
    }

    @Override
    public boolean dirExists(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(dirURI);
        return dir.exists() && dir.isDirectory();
    }

    @Override
    public boolean removeFile(String fileURI)  {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        File f = new File(fileURI);
        return f.delete();
    }

    @Override
    public String[] listDirectory(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(dirURI);
        return dir.list();
    }


    @Override
    public boolean renameFile(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);
        return file1.renameTo(file2);
    }

    @Override
    public long fileSize(String path) {
        File file = new File(path);
        return file.length();
    }

    @Override
    public long fileAvailableSize(String fileURI) throws IOException {
        return new File(fileURI).getFreeSpace();
    }

    @Override
    public boolean makeDirectory(String dirPath) throws IOException {
        File newDir = new File(dirPath);
        return newDir.mkdir();
    }

    @Override
    public boolean makeDirectoryRecursive(String dirURI) throws IOException {
        return new File(dirURI).mkdirs();
    }

    @Override
    public boolean removeRecursively(String path) {
        return removeRecursively(new File(path));
    }

    public boolean removeRecursively(File f) {
        if(f.isDirectory()) {
            File[] dirContents = f.listFiles();
            for(int i = 0; i < dirContents.length; i++) {
                if(dirContents[i].isDirectory()) {
                    removeRecursively(dirContents[i]);
                }
                dirContents[i].delete();
            }
        }
        return f.delete();
    }

    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        return parser;
    }

    public XmlSerializer newXMLSerializer() {
        XmlSerializer serializer = null;
        try {
            if(xmlPullParserFactory == null) {
                xmlPullParserFactory = XmlPullParserFactory.newInstance();
            }

            serializer = xmlPullParserFactory.newSerializer();
        }catch(XmlPullParserException e) {
            l(UMLog.ERROR, 92, null, e);
        }

        return serializer;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ZipFileHandle openZip(String name) throws IOException{
        return new ZipFileHandleSharedSE(name);
    }

    /**
     * @{inheritDoc}
     */
    public String hashAuth(Object context, String auth) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(auth.getBytes());
            byte[] digest = md.digest();
            return new String(Base64Coder.encode(digest));
        }catch(NoSuchAlgorithmException e) {
            l(UMLog.ERROR, 86, null, e);
        }

        return null;
    }

    /**
     * Return the network manager for this platform
     *
     * @return
     */
    public abstract NetworkManager getNetworkManager();

    @Override
    public void setActiveUser(String username, Object context) {
        super.setActiveUser(username, context);
//        TODO: handle
//        xapiAgent = username != null ? XapiAgentEndpoint.createOrUpdate(context, null, username,
//                UMTinCanUtil.getXapiServer(context)) : null;

        fireActiveUserChangedEvent(username, context);
    }

    @Override
    public CourseProgress getCourseProgress(String[] entryIds, Object context) {
        if(getActiveUser(context) == null)
            return null;

        return null;

//        XapiStatementManager stmtManager = PersistenceManager.getInstance().getManager(XapiStatementManager.class);
//
//        String[] entryIdsPrefixed = new String[entryIds.length];
//        for(int i = 0; i < entryIdsPrefixed.length; i++) {
//            entryIdsPrefixed[i] = "epub:" + entryIds[i];
//        }
//
//        List<? extends XapiStatement> progressStmts = stmtManager.findByProgress(context,
//                entryIdsPrefixed, getCurrentAgent(), null, new String[]{
//                    UMTinCanUtil.VERB_ANSWERED, UMTinCanUtil.VERB_PASSED, UMTinCanUtil.VERB_FAILED
//                }, 1);
//
//        if(progressStmts.size() == 0) {
//            return new CourseProgress(CourseProgress.STATUS_NOT_STARTED, 0, 0);
//        }else {
//            XapiStatement stmt = progressStmts.get(0);
//            String stmtVerb = stmt.getVerb().getVerbId();
//            CourseProgress courseProgress = new CourseProgress();
//            if(stmtVerb.equals(UMTinCanUtil.VERB_ANSWERED))
//                courseProgress.setStatus(MessageID.in_progress);
//            else if(stmtVerb.equals(UMTinCanUtil.VERB_PASSED))
//                courseProgress.setStatus(MessageID.passed);
//            else if(stmtVerb.equals(UMTinCanUtil.VERB_FAILED))
//                courseProgress.setStatus(MessageID.failed_message);
//
//            courseProgress.setProgress(stmt.getResultProgress());
//            courseProgress.setScore(Math.round(stmt.getResultScoreScaled()));
//
//            return courseProgress;
//        }
    }


    public void addActiveUserListener(ActiveUserListener listener) {
        activeUserListener.addElement(listener);
    }

    public void removeActiveUserListener(ActiveUserListener listener) {
        activeUserListener.removeElement(listener);
    }

    protected void fireActiveUserChangedEvent(String username, Object context) {
        for(int i = 0; i < activeUserListener.size(); i++) {
            ((ActiveUserListener)activeUserListener
                    .elementAt(i)).userChanged(username, context);
        }
    }

    protected void fireActiveUserCredChangedEvent(String cred, Object context) {
        for(int i = 0; i < activeUserListener.size(); i++) {
            ((ActiveUserListener)activeUserListener
                    .elementAt(i)).credChanged(cred, context);
        }
    }

    //ActiveSyncListener:
    //TODO: Check if gotta remove this.

    public void addActiveSyncListener(ActiveSyncListener listener){
        activeSyncListener.addElement(listener);
    }

    public void removeActiveSyncListener(ActiveSyncListener listener){
        activeSyncListener.removeElement(listener);
    }

    public void fireSetSyncHappeningEvent(boolean happening, Object context){
        for(int i = 0; i < activeSyncListener.size(); i++) {
            ( (ActiveSyncListener)
                    activeSyncListener.elementAt(i)
            ).setSyncHappening(happening, context);
        }

    }

    @Override
    public String formatInteger(int integer) {
        return NumberFormat.getIntegerInstance().format(integer);
    }

    @Override
    public UmHttpCall makeRequestAsync(UmHttpRequest request, final UmHttpResponseCallback callback) {
        Request.Builder httpRequest = new Request.Builder().url(request.getUrl());
        if(request.getHeaders() != null) {
            Enumeration allHeaders = request.getHeaders().keys();
            String header;
            while(allHeaders.hasMoreElements()) {
                header = (String)allHeaders.nextElement();
                httpRequest.addHeader(header, (String)request.getHeaders().get(header));
            }
        }

        Call call = client.newCall(httpRequest.build());
        final UmHttpCall umCall = new UmHttpCallSe(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(umCall, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onComplete(umCall, new UmHttpResponseSe(response));
            }
        });

        return umCall;
    }

    @Override
    public UmHttpCall sendRequestAsync(UmHttpRequest request, UmHttpResponseCallback responseListener) {
        return makeRequestAsync(request, responseListener);
    }

    @Override
    protected UmHttpResponse sendRequestSync(UmHttpRequest request) throws IOException{
        Request.Builder httpRequest = new Request.Builder().url(request.getUrl());
        Call call = client.newCall(httpRequest.build());
        return new UmHttpResponseSe(call.execute());
    }

    @Override
    public UmHttpResponse makeRequestSync(UmHttpRequest request) throws IOException {
        return getHttpCache(request.getContext()).getSync(request);
    }

    @Override
    public HttpCache getHttpCache(Object context) {
        if(httpCache == null)
            httpCache = new HttpCache(getCacheDir(CatalogPresenter.SHARED_RESOURCE, context));

        return httpCache;
    }



    @Override
    public String convertTimeToReadableTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }

    public abstract InputStream getAssetSync(Object context, String path) throws IOException;

    @Override
    public String getAppConfigString(String key, String defaultVal, Object context) {
        if(appConfig == null) {
            String appPrefResource = getManifestPreference("com.ustadmobile.core.appconfig",
                    "/com/ustadmobile/core/appconfig.properties", context);
            appConfig = new Properties();
            InputStream prefIn = null;

            try {
                prefIn = getAssetSync(context, appPrefResource);
                appConfig.load(prefIn);
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 685, appPrefResource, e);
            }finally {
                UMIOUtils.closeInputStream(prefIn);
            }
        }

        return appConfig.getProperty(key, defaultVal);
    }

    @Override
    public OpdsAtomFeedRepository getOpdsAtomFeedRepository(Object context) {
        if(atomFeedRepository == null) {
            atomFeedRepository = new OpdsAtomFeedRepositoryImpl(UmAppDatabase.getInstance(context),
                    Executors.newCachedThreadPool());
        }

        return atomFeedRepository;
    }
}
