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

import org.json.JSONObject;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Created by mike on 4/25/17.
 */

public class UstadMobileSystemImplTest extends UstadMobileSystemImpl {
    @Override
    public void go(String viewName, Hashtable args, Object context) {

    }

    @Override
    public boolean loadActiveUserInfo(Object context) {
        return false;
    }

    @Override
    public String getImplementationName() {
        return null;
    }

    @Override
    public boolean isJavascriptSupported() {
        return false;
    }

    @Override
    public boolean isHttpsSupported() {
        return false;
    }

    @Override
    public boolean queueTinCanStatement(JSONObject stmt, Object context) {
        return false;
    }

    @Override
    public void addTinCanQueueStatusListener(TinCanQueueListener listener) {

    }

    @Override
    public void removeTinCanQueueListener(TinCanQueueListener listener) {

    }

    @Override
    public String getCacheDir(int mode, Object context) {
        return null;
    }

    @Override
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        return new UMStorageDir[0];
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
    public String getSystemLocale(Object context) {
        return null;
    }

    @Override
    public Hashtable getSystemInfo() {
        return null;
    }

    @Override
    public long fileLastModified(String fileURI) {
        return 0;
    }

    @Override
    public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException, SecurityException {
        return null;
    }

    @Override
    public InputStream openFileInputStream(String fileURI) throws IOException, SecurityException {
        return null;
    }

    @Override
    public boolean fileExists(String fileURI) throws IOException {
        return false;
    }

    @Override
    public boolean dirExists(String dirURI) throws IOException {
        return false;
    }

    @Override
    public boolean removeFile(String fileURI) {
        return false;
    }

    @Override
    public String[] listDirectory(String dirURI) throws IOException {
        return new String[0];
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
    public boolean renameFile(String fromFileURI, String toFileURI) {
        return false;
    }

    @Override
    public long fileSize(String fileURI) {
        return 0;
    }

    @Override
    public long fileAvailableSize(String fileURI) throws IOException {
        return 0;
    }

    @Override
    public boolean makeDirectory(String dirURI) throws IOException {
        return false;
    }

    @Override
    public boolean removeRecursively(String dirURI) {
        return false;
    }

    @Override
    public boolean makeDirectoryRecursive(String dirURI) throws IOException {
        return false;
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
    public HTTPResult makeRequest(String url, Hashtable headers, Hashtable postParameters, String method, byte[] postBody) throws IOException {
        return null;
    }

    @Override
    public XmlPullParser newPullParser() throws XmlPullParserException {
        return new KXmlParser();
    }

    @Override
    public XmlSerializer newXMLSerializer() {
        return new KXmlSerializer();
    }

    @Override
    public AppView getAppView(Object context) {
        return null;
    }

    @Override
    public UMLog getLogger() {
        return null;
    }

    @Override
    public ZipFileHandle openZip(String name) throws IOException {
        return null;
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

    @Override
    public String hashAuth(Object context, String auth) {
        return null;
    }
}
