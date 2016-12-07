/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.sharedse.impl;

import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.TinCanQueueListener;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.Base64Coder;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
<<<<<<< HEAD
import com.ustadmobile.core.view.CatalogView;
=======
>>>>>>> master

import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.port.sharedse.impl.zip.*;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author mike
 */
public abstract class UstadMobileSystemImplSE extends UstadMobileSystemImpl {

    private XmlPullParserFactory xmlPullParserFactory;

    /**
     * @inheritDoc
     */
    @Override
    public HTTPResult makeRequest(String httpURL, Hashtable headers, Hashtable postParams, String method, byte[] postBody) throws IOException {
<<<<<<< HEAD
        URL url = new URL(httpURL);
        HttpURLConnection conn = (HttpURLConnection)openConnection(url);

        if(headers != null) {
            Enumeration e = headers.keys();
            while(e.hasMoreElements()) {
                String headerField = e.nextElement().toString();
                String headerValue = headers.get(headerField).toString();
                conn.setRequestProperty(headerField, headerValue);
            }
        }
        //conn.setRequestProperty("Connection", "close");

        conn.setRequestMethod(method);
=======
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        HTTPResult result = null;
        
        try {
            URL url = new URL(httpURL);
            conn = (HttpURLConnection)url.openConnection();
>>>>>>> master

            if(headers != null) {
                Enumeration e = headers.keys();
                while(e.hasMoreElements()) {
                    String headerField = e.nextElement().toString();
                    String headerValue = headers.get(headerField).toString();
                    conn.setRequestProperty(headerField, headerValue);
                }
            }

            conn.setRequestMethod(method);

            if("POST".equals(method)) {
                if(postBody == null && postParams != null && postParams.size() > 0) {
                    //we need to write the post params to the request
                    StringBuilder sb = new StringBuilder();
                    Enumeration e = postParams.keys();
                    boolean firstParam = true;
                    while(e.hasMoreElements()) {
                        String key = e.nextElement().toString();
                        String value = postParams.get(key).toString();
                        if(firstParam) {
                            firstParam = false;
                        }else {
                            sb.append('&');
                        }
                        sb.append(URLEncoder.encode(key, "UTF-8")).append('=');
                        sb.append(URLEncoder.encode(value, "UTF-8"));
                    }

                    postBody = sb.toString().getBytes();
                }else if(postBody == null) {
                    throw new IllegalArgumentException("Cant make a post request with no body and no parameters");
                }

                conn.setDoOutput(true);
                out = conn.getOutputStream();
                out.write(postBody);
                out.flush();
                out.close();
            }   

            conn.connect();

            int statusCode = conn.getResponseCode();
            //on iOS this will not throw an exception but will have an response code of <= 0
            if(statusCode <= 0) {
                throw new IOException("HTTP Exception: status < 0" + statusCode);
            }
            
            int contentLen = conn.getContentLength();
            
            
            in = statusCode < 400 ? conn.getInputStream() : conn.getErrorStream();
            byte[] buf = new byte[1024];
            int bytesRead = 0;
            int bytesReadTotal = 0;

            //do not read more bytes than is available in the stream
            int bytesToRead = Math.min(buf.length, contentLen != -1 ? contentLen : buf.length);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            if(!method.equalsIgnoreCase("HEAD")) {
                while((contentLen != -1 ? (bytesRead < contentLen) : true)  && (bytesRead = in.read(buf, 0, contentLen == -1 ? buf.length : Math.min(buf.length, contentLen - bytesRead))) != -1) {
                    bout.write(buf, 0, bytesRead);
                }
            }

            in.close();

            Hashtable responseHeaders = new Hashtable();
            Iterator<String> headerIterator = conn.getHeaderFields().keySet().iterator();
            while(headerIterator.hasNext()) {
                String header = headerIterator.next();
                if(header == null) {
                    continue;//a null header is the response line not header; leave that alone...
                }

                String headerVal = conn.getHeaderField(header);
                responseHeaders.put(header.toLowerCase(), headerVal);
            }

            byte[] resultBytes = bout.toByteArray();
            result = new HTTPResult(resultBytes, statusCode,
                    responseHeaders);
        }catch(IOException e) {
            l(UMLog.ERROR, 80, httpURL, e);
        }finally {
            UMIOUtils.closeOutputStream(out);
            UMIOUtils.closeInputStream(in);
            
            if(conn != null) {
                conn.disconnect();
            }
        }
        
        return result;
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
    protected abstract String getSystemBaseDir();

    @Override
    public String getCacheDir(int mode, Object context) {
        String systemBaseDir = getSystemBaseDir();
        if(mode == CatalogController.SHARED_RESOURCE) {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, UstadMobileConstants.CACHEDIR});
        }else {
            return UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-" + getActiveUser(context),
                    UstadMobileConstants.CACHEDIR});
        }
    }

    @Override
    public UMStorageDir[] getStorageDirs(int mode, Object context) {
        List<UMStorageDir> dirList = new ArrayList<>();
        String systemBaseDir = getSystemBaseDir();

        if((mode & CatalogController.SHARED_RESOURCE) == CatalogController.SHARED_RESOURCE) {
            dirList.add(new UMStorageDir(systemBaseDir, getString(MessageIDConstants.device), false, true, false));

            //Find external directories
            String[] externalDirs = findRemovableStorage();
            for(String extDir : externalDirs) {
                dirList.add(new UMStorageDir(UMFileUtil.joinPaths(new String[]{extDir,
                        UstadMobileSystemImpl.CONTENT_DIR_NAME}), getString(MessageIDConstants.memory_card),
                        true, true, false, false));
            }
        }

        if((mode & CatalogController.USER_RESOURCE) == CatalogController.USER_RESOURCE) {
            String userBase = UMFileUtil.joinPaths(new String[]{systemBaseDir, "user-"
                    + getActiveUser(context)});
            dirList.add(new UMStorageDir(userBase, getString(MessageIDConstants.device), false, true, true));
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
        return new File(resolveFileUriToPath(fileURI)).lastModified();
    }

    @Override
    public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileOutputStream(resolveFileUriToPath(fileURI), (flags & FILE_APPEND) == FILE_APPEND);
    }

    @Override
    public InputStream openFileInputStream(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new FileInputStream(resolveFileUriToPath(fileURI));
    }


    @Override
    public boolean fileExists(String fileURI) throws IOException {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        return new File(resolveFileUriToPath(fileURI)).exists();
    }

    @Override
    public boolean dirExists(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(resolveFileUriToPath(dirURI));
        return dir.exists() && dir.isDirectory();
    }

    @Override
    public boolean removeFile(String fileURI)  {
        fileURI = UMFileUtil.stripPrefixIfPresent("file://", fileURI);
        File f = new File(resolveFileUriToPath(fileURI));
        return f.delete();
    }

    @Override
    public String[] listDirectory(String dirURI) throws IOException {
        dirURI = UMFileUtil.stripPrefixIfPresent("file://", dirURI);
        File dir = new File(resolveFileUriToPath(dirURI));
        return dir.list();
    }


    @Override
    public boolean renameFile(String path1, String path2) {
        File file1 = new File(resolveFileUriToPath(path1));
        File file2 = new File(resolveFileUriToPath(path2));
        return file1.renameTo(file2);
    }

    @Override
    public long fileSize(String path) {
        File file = new File(resolveFileUriToPath(path));
        return file.length();
    }

    @Override
    public long fileAvailableSize(String fileURI) throws IOException {
        return new File(resolveFileUriToPath(fileURI)).getFreeSpace();
    }

    @Override
    public boolean makeDirectory(String dirPath) throws IOException {
        File newDir = new File(resolveFileUriToPath(dirPath));
        return newDir.mkdir();
    }

    @Override
    public boolean makeDirectoryRecursive(String dirURI) throws IOException {
        return new File(resolveFileUriToPath(dirURI)).mkdirs();
    }

    @Override
    public boolean removeRecursively(String path) {
        return removeRecursively(new File(resolveFileUriToPath(path)));
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
        return new ZipFileHandleSharedSE(resolveFileUriToPath(name));
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


}
