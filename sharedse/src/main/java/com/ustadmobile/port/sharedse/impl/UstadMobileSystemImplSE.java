/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.TinCanQueueListener;
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
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.nanolrs.core.endpoints.XapiAgentEndpoint;
import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.NodeSyncStatusManager;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiStatementManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.NodeSyncStatus;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.model.XapiStatement;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.sync.UMSyncEndpoint;
import com.ustadmobile.nanolrs.core.sync.UMSyncResult;
import com.ustadmobile.port.sharedse.impl.http.UmHttpCallSe;
import com.ustadmobile.port.sharedse.impl.http.UmHttpResponseSe;
import com.ustadmobile.port.sharedse.impl.zip.ZipFileHandleSharedSE;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import org.json.JSONObject;
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import listener.ActiveSyncListener;
import listener.ActiveUserListener;
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

    protected XapiAgent xapiAgent;

    Vector activeUserListener = new Vector();
    Vector activeSyncListener = new Vector();
    //ActiveSyncListener activeSyncListener;

    private HttpCache httpCache;

    private final OkHttpClient client = new OkHttpClient();

    public static String DEFAULT_MAIN_SERVER_HOST_NAME = "umcloud1svlt";

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

    @Override
    public boolean queueTinCanStatement(final JSONObject stmt, final Object context) {
        //Placeholder for iOS usage
        return false;
    }

    public void addTinCanQueueStatusListener(final TinCanQueueListener listener) {
        //TODO: remove this - it's not really used - do nothing
    }

    public void removeTinCanQueueListener(TinCanQueueListener listener) {
        //TODO: remove this - it's not really used - do nothing
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

        if((mode & CatalogPresenter.SHARED_RESOURCE) == CatalogPresenter.SHARED_RESOURCE) {
            dirList.add(new UMStorageDir(systemBaseDir, getString(MessageID.device, context),
                    false, true, false));

            //Find external directories
            String[] externalDirs = findRemovableStorage();
            for(String extDir : externalDirs) {
                dirList.add(new UMStorageDir(UMFileUtil.joinPaths(new String[]{extDir,
                        UstadMobileSystemImpl.CONTENT_DIR_NAME}),
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

    protected XapiAgent getCurrentAgent() {
        //This is set with setActiveUser
        return xapiAgent;
    }

    @Override
    public void setActiveUser(String username, Object context) {
        super.setActiveUser(username, context);
        xapiAgent = username != null ? XapiAgentEndpoint.createOrUpdate(context, null, username,
                UMTinCanUtil.getXapiServer(context)) : null;

        fireActiveUserChangedEvent(username, context);
    }

    @Override
    public CourseProgress getCourseProgress(String[] entryIds, Object context) {
        if(getActiveUser(context) == null)
            return null;

        XapiStatementManager stmtManager = PersistenceManager.getInstance().getManager(XapiStatementManager.class);

        String[] entryIdsPrefixed = new String[entryIds.length];
        for(int i = 0; i < entryIdsPrefixed.length; i++) {
            entryIdsPrefixed[i] = "epub:" + entryIds[i];
        }

        List<? extends XapiStatement> progressStmts = stmtManager.findByProgress(context,
                entryIdsPrefixed, getCurrentAgent(), null, new String[]{
                    UMTinCanUtil.VERB_ANSWERED, UMTinCanUtil.VERB_PASSED, UMTinCanUtil.VERB_FAILED
                }, 1);

        if(progressStmts.size() == 0) {
            return new CourseProgress(CourseProgress.STATUS_NOT_STARTED, 0, 0);
        }else {
            XapiStatement stmt = progressStmts.get(0);
            String stmtVerb = stmt.getVerb().getVerbId();
            CourseProgress courseProgress = new CourseProgress();
            if(stmtVerb.equals(UMTinCanUtil.VERB_ANSWERED))
                courseProgress.setStatus(MessageID.in_progress);
            else if(stmtVerb.equals(UMTinCanUtil.VERB_PASSED))
                courseProgress.setStatus(MessageID.passed);
            else if(stmtVerb.equals(UMTinCanUtil.VERB_FAILED))
                courseProgress.setStatus(MessageID.failed_message);

            courseProgress.setProgress(stmt.getResultProgress());
            courseProgress.setScore(Math.round(stmt.getResultScoreScaled()));

            return courseProgress;
        }
    }

    @Override
    public int registerUser(String username, String password, Hashtable fields, Object context) {
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        String loggedInUsername = null;
        loggedInUsername = UstadMobileSystemImpl.getInstance().getActiveUser(context);
        //ignore loggedInUsername cause if we're clicking register, we want this user
        //to log in..

        User loggedInUser = null;
        String cred = password;
        //List<User> users = userManager.findByUsername(context, username);
        //if(users!= null && !users.isEmpty()){
        //    loggedInUser = users.get(0);
        loggedInUser = userManager.findByUsername(context, username);
        if(loggedInUser == null){
            //create the user
            try {
                loggedInUser = (User)userManager.makeNew();
                loggedInUser.setUsername(username);
                loggedInUser.setUuid(UUID.randomUUID().toString());

                //TODO: Test this new way
                if(password != null && !password.isEmpty()){
                    try {
                        //hash it.
                        password = userManager.hashPassword(password);
                    } catch (NoSuchAlgorithmException e) {
                        System.out.println("Cannot hash password.: " + e);
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                loggedInUser.setPassword(password);

                loggedInUser.setNotes("User Created via Registration Page");
                loggedInUser.setDateCreated(System.currentTimeMillis());
                loggedInUser.setMasterSequence(-1); //This is needed to check is new user or not.
                //However, normal persist will over ride this to local sequence.
                ChangeSeqManager changeSeqManager =
                        PersistenceManager.getInstance().getManager(ChangeSeqManager.class);
                String tableName = UMSyncEndpoint.getTableNameFromClass(User.class);
                long setThis = changeSeqManager.getNextChangeAddSeqByTableName(tableName, 1, context);
                loggedInUser.setMasterSequence(-1);
                loggedInUser.setLocalSequence(setThis);
                userManager.persist(context, loggedInUser, false);
                userCustomFieldsManager.createUserCustom(fields,loggedInUser, context);
            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        }

        //handleUserLoginAuthComplete(loggedInUser.getUsername(), loggedInUser.getPassword(), context);
        //Specifying the clear text password not the hashed persited one.
        handleUserLoginAuthComplete(loggedInUser.getUsername(), cred, context);
        return 0;
    }

    /**
     * Update users custom fields or make new ones..
     * @param map
     * @param user
     * @param dbContext
     * @throws SQLException
     */
    public void updateUserCustom(Map<Integer, String> map, User user, Object dbContext)
            throws SQLException {
        UserCustomFieldsManager customFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        Set<Map.Entry<Integer, String>> es = map.entrySet();
        Iterator<Map.Entry<Integer, String>> it = es.iterator();

        List<UserCustomFields> userFields = customFieldsManager.findByUser(user, dbContext);

        while(it.hasNext()){
            Map.Entry<Integer, String> e = it.next();
            int key = e.getKey();
            String value = e.getValue(); //new value
            boolean fieldExists = false;
            UserCustomFields uce = null;
            for(UserCustomFields customField : userFields){
                if(customField.getFieldName() == key){
                    uce = customField;
                    fieldExists = true;
                    uce.setFieldValue(value);
                    break;
                }
            }
            if(fieldExists == false){
                uce = (UserCustomFields)customFieldsManager.makeNew();
                uce.setUuid(UUID.randomUUID().toString());
                if(user!=null) {
                    uce.setUser(user);
                }
                uce.setFieldName(key);
                uce.setFieldValue(value);
            }

            customFieldsManager.persist(dbContext, uce);
        }
    }

    @Override
    public int updateUser(String username, String password, Hashtable fields, Object context) {
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        User loggedInUser = null;
        loggedInUser = userManager.findByUsername(context, username);
        if(loggedInUser != null){
            //update the user
            try {
                loggedInUser.setNotes("User Updated via Account Settings Page");
                userManager.persist(context, loggedInUser, true);
                //userCustomFieldsManager.createUserCustom(fields,loggedInUser, context);
                updateUserCustom(fields,loggedInUser, context);

            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        }

        handleUserLoginAuthComplete(loggedInUser.getUsername(), loggedInUser.getPassword(), context);
        return 0;
    }

    /**
     * Utility merge of what happens after a user is logged in through username/password
     * and what happens after they are newly registered etc.
     */
    private void handleUserLoginAuthComplete(final String username, final String password, Object dbContext) {
        //Updates user to pref. and userobject to Service.
        setActiveUser(username, dbContext);

        //Updates password cred to pref.
        setActiveUserAuth(password, dbContext);

        //Updating password to Service as well.
        fireActiveUserCredChangedEvent(password, dbContext);

        String authHashed = hashAuth(dbContext, password);
        setAppPref("um-authcache-" + username, authHashed, dbContext);

    }

    @Override
    public boolean handleLoginLocally(String username, String password, Object dbContext) {
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        //boolean result = userManager.authenticate(dbContext, username, password);
        //Authenticating and hashing it.
        boolean result = userManager.authenticate(dbContext, username, password, true);
        if(result){
            handleUserLoginAuthComplete(username, password, dbContext);
        }
        return result;

    }

    @Override
    public boolean createUserLocally(String username, String password, String uuid, Object dbContext) {
        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        try {
            User user = (User) userManager.makeNew();
            if(uuid != null && !uuid.isEmpty()){
                user.setUuid(uuid);
            }else{
                user.setUuid(UUID.randomUUID().toString());
            }
            user.setUsername(username);

            //TODO: Test this.
            if(password != null && !password.isEmpty()){
                try {
                    //hash it.
                    password = userManager.hashPassword(password);
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Cannot hash password.: " + e);
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            user.setPassword(password);
            userManager.persist(dbContext, user);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        return httpCache.getSync(request);
    }

    @Override
    public HttpCache getHttpCache(Object context) {
        return httpCache;
    }

    /**
     * Get list of syncs
     * @param context
     * @return
     */
    @Override
    public LinkedHashMap<String, String> getSyncHistory(Object nodeObj, Object context) {
        NodeSyncStatusManager nodeSyncStatusManager =
                PersistenceManager.getInstance().getManager(NodeSyncStatusManager.class);
        LinkedHashMap syncHistoryMap = new LinkedHashMap<>();
        Node node = (Node) nodeObj;
        try {
            List<NodeSyncStatus> allStatuses = nodeSyncStatusManager.getStatusesByNode(context, node);
            Iterator<NodeSyncStatus> allStatusesIterator = allStatuses.iterator();
            while(allStatusesIterator.hasNext()){
                NodeSyncStatus thisNodeStatus = allStatusesIterator.next();
                String thisNodeStatusResult = "SUCCESS";
                if(!thisNodeStatus.getSyncResult().equals("200")){
                    thisNodeStatusResult = "FAIL";
                }
                syncHistoryMap.put(thisNodeStatus.getSyncDate(), thisNodeStatusResult);
            }
            return syncHistoryMap;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LinkedHashMap<String, String> getMainNodeSyncHistory(Object context) {
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        Node mainNode = null;
        try {
            mainNode = nodeManager.getMainNode(DEFAULT_MAIN_SERVER_HOST_NAME, context);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return getSyncHistory(mainNode, context);
    }

    @Override
    public long getMainNodeLastSyncDate(Object context) {
        NodeSyncStatusManager nodeSyncStatusManager =
                PersistenceManager.getInstance().getManager(NodeSyncStatusManager.class);
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);
        Node mainNode = null;
        NodeSyncStatus latestMainNodeSync = null;
        try {
            mainNode = nodeManager.getMainNode(
                    DEFAULT_MAIN_SERVER_HOST_NAME, context);

            latestMainNodeSync =
                    nodeSyncStatusManager.getLatestSuccessfulStatusByNode(context, mainNode);
            long lastSyncDate = latestMainNodeSync.getSyncDate();
            return lastSyncDate;
        }catch(SQLException se){
            se.printStackTrace();
            return 0;
        }
    }

    @Override
    public void triggerSync(final Object context) throws Exception {

        UserManager userManager = PersistenceManager.getInstance().getManager(UserManager.class);
        NodeManager nodeManager = PersistenceManager.getInstance().getManager(NodeManager.class);

        String loggedInUsername = getActiveUser(context);
        User loggedInUser = userManager.findByUsername(context, loggedInUsername);
        String loggedInUserCred = getActiveUserAuth(context);
        Node endNode = null;

        try {
            endNode = nodeManager.getMainNode(DEFAULT_MAIN_SERVER_HOST_NAME, context);
            UMSyncResult result = UMSyncEndpoint.startSync(loggedInUser, loggedInUserCred,
                    endNode, context);
            if(result.getStatus() > -1){
                UstadMobileSystemImpl.getInstance().fireSetSyncHappeningEvent(false, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Update view that something went wrong TODO
        }

    }

    @Override
    public String convertTimeToReadableTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }
}
