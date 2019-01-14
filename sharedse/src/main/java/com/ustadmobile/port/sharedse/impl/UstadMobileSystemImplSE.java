/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.fs.db.repository.OpdsAtomFeedRepositoryImpl;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.impl.http.UmHttpCallSe;
import com.ustadmobile.port.sharedse.impl.http.UmHttpResponseSe;
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

    private HttpCache httpCache;

    private final OkHttpClient client = new OkHttpClient();

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
            httpCache = new HttpCache(getCacheDir(SHARED_RESOURCE, context));
    }

    /**
     * Open the given connection and return the HttpURLConnection object using a proxy if required
     *
     * @param url
     *
     * @return
     */
    public abstract URLConnection openConnection(URL url) throws IOException;

    /**
     * Returns the system base directory to work from
     *
     * @return
     */
    protected abstract String getSystemBaseDir(Object context);


    @Override
    public String getCacheDir(int mode, Object context) {
        String systemBaseDir = getSystemBaseDir(context);
        return UMFileUtil.joinPaths(new String[]{systemBaseDir, UstadMobileConstants.CACHEDIR});
    }

    @Override
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        List<UMStorageDir> dirList = new ArrayList<>();
        String systemBaseDir = getSystemBaseDir(context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        final String contentDirName = getContentDirName(context);

        if((mode & SHARED_RESOURCE) == SHARED_RESOURCE) {
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

        UmAccount account = UmAccountManager.getActiveAccount(context);
        if(account != null
                && ((mode & UstadMobileSystemImpl.USER_RESOURCE) == UstadMobileSystemImpl.USER_RESOURCE)) {
            String userBase = UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-",
                    account.getUsername()});
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
            httpCache = new HttpCache(getCacheDir(SHARED_RESOURCE, context));

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
