/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.port.j2me.impl;

//import com.ustadmobile.app.controller.UstadMobileAppController;
import com.sun.lwuit.Form;
import com.ustadmobile.port.j2me.app.AppPref;
import com.ustadmobile.port.j2me.app.DeviceRoots;
import com.ustadmobile.port.j2me.app.FileUtils;
import com.ustadmobile.port.j2me.app.HTTPUtils;
import com.ustadmobile.core.impl.UMTransferJob;
import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.j2me.app.UserPref;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMProgressEvent;
import com.ustadmobile.core.impl.UMProgressListener;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.opds.UstadJSOPDSEntry;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import com.ustadmobile.port.j2me.view.AppViewJ2ME;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author varuna
 */
public class UstadMobileSystemImplJ2ME  extends UstadMobileSystemImpl {

    private UMLog umLogger;
    
    private AppViewJ2ME appView;
    
    private Form currentForm;
    
    public String getImplementationName() {
        return "J2ME";
    }

    public UstadMobileSystemImplJ2ME() {
        umLogger = new UMLogJ2ME();
        appView = new AppViewJ2ME(this);
    }
    
    public static UstadMobileSystemImplJ2ME getInstanceJ2ME() {
        return (UstadMobileSystemImplJ2ME)mainInstance;
    }
    
    /**
     * This needs to be called so the system knows the current form
     * @param frm 
     */
    public void handleFormShow(Form frm) {
        if(this.currentForm != frm) {
            appView.dismissAll();
        }
        this.currentForm = frm;
    }
    
    public Form getCurrentForm() {
        return currentForm;
    }

    public boolean dirExists(String dirURI) throws IOException {
        return FileUtils.checkDir(dirURI);
    }

    /**
     * @inheritDoc
     */
    public UMTransferJob downloadURLToFile(String url, String fileURI, 
            Hashtable headers) {
        return new DownloadJob(url, fileURI, this);
    }

    public void setActiveUser(String username) {
        AppPref.addSetting("CURRENTUSER", username);
        UserPref.setActiveUser(username);
        
    }

    public void setUserPref(String key, String value) {
        UserPref.addSetting(key, value);
    }

    public void saveUserPrefs() {
        
    }
    
    /**
     * Provides the path to the shared content directory 
     * 
     * @return URI of the shared content directory
     */
    public String getSharedContentDir(){ 
        //This will be in something like ustadmobileContent
        //appData is different
        try{
            DeviceRoots dt = FileUtils.getBestRoot();
            String sharedContentDir = FileUtils.joinPath(dt.path, 
                    FileUtils.USTAD_CONTENT_DIR);
            
            //Check if it is created. If it isnt, create it.       
            if(FileUtils.createFileOrDir(sharedContentDir, 
                    Connector.READ_WRITE, true)){
                return sharedContentDir;
            }
            
            //Return null if it doens't exist.
            if (!FileUtils.checkDir(sharedContentDir)){
                return null;
            }
        }catch (Exception e){}
        return null;
    }
    
    public String getUserContentDirectory(String username){
        try{
            DeviceRoots dt = FileUtils.getBestRoot();
            String sharedUserContentDir = dt.path + FileUtils.FILE_SEP + 
                    FileUtils.USTAD_CONTENT_DIR + FileUtils.FILE_SEP + username;
            
            //Return null if it doesn't exist
            if (!FileUtils.checkDir(sharedUserContentDir)){
                return null;
            }
            
            //Check if it is created. If it isn't, create it.
            if(FileUtils.createFileOrDir(sharedUserContentDir, 
                    Connector.READ_WRITE, true)){
                return sharedUserContentDir;
            }
            
        }catch (Exception e){}
        return null;
    }
    
    public String getSystemLocale(){
        return System.getProperty("microedition.locale").toString();
    }
    
    public Hashtable getSystemInfo(){
        Hashtable systemInfo = new Hashtable();
        try{
            systemInfo.put("platform", 
                    System.getProperty("microedition.platform").toString());
            systemInfo.put("encoding", 
                    System.getProperty("microedition.encoding").toString());
            systemInfo.put("configuration", 
                    System.getProperty("microedition.configuration").toString());
            systemInfo.put("profiles", 
                    System.getProperty("microedition.profiles").toString());
            systemInfo.put("locale", 
                    System.getProperty("microedition.locale").toString());
            systemInfo.put("memorytotal", 
                    Long.toString(Runtime.getRuntime().totalMemory()));
            systemInfo.put("memoryfree", 
                    Long.toString(Runtime.getRuntime().freeMemory()));
            return systemInfo;
        }catch (Exception e){}
        return null;
    }
    
    public String readFileAsText(String fileURI, String encoding) 
            throws IOException{
        try{
            String contents = FileUtils.getFileContents(fileURI);
            return contents;
        }catch(Exception e){}
        return null;
    }
    
    public String readFileAsText(String fileURI) throws IOException{
        return this.readFileAsText(fileURI, "UTF-8");
    }
    
    public long modTimeDifference(String fileURI1, String fileURI2){
        try{
            long file1LastModified = FileUtils.getLastModified(fileURI1);
            long file2LastModified = FileUtils.getLastModified(fileURI2);
            if (file1LastModified != -1 || file2LastModified != -1 ){
                long difference = file1LastModified - file2LastModified;
                return difference;
            }
        }catch(Exception e){}
        return -1;
    }
    
    public void writeStringToFile(String str, String fileURI, String encoding) 
            throws IOException{
        try{
            FileUtils.writeStringToFile(str, fileURI, true);
            
        }catch (Exception e){}
    }
    
    public boolean fileExists(String fileURI) throws IOException{
        return FileUtils.checkFile(fileURI);
    }
    
    public boolean removeFile(String fileURI) {
        boolean success = false;
        try {
            success = FileUtils.removeFileOrDir(fileURI, Connector.READ_WRITE,
            false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (success == false){
            //Wanna do something?
        }
        return success;
    }

    public String[] listDirectory(String dirURI) throws IOException{
        
        String[] list = FileUtils.listFilesInDirectory(dirURI);
        
        return list;
    }
    
    public boolean renameFile(String fromFileURI, String toFileURI){
        boolean success = false;
        try {
            success = FileUtils.renameFileOrDir(fromFileURI, toFileURI, 
            Connector.READ_WRITE, false);
            if (success == false){
                //Wanna do something?
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return success;
       
    }
    
    public long fileSize(String fileURI){
        try{
            return FileUtils.getFileSize(fileURI);
        }catch(Exception e){}
        return -1;
    }
    
    public boolean makeDirectory(String dirURI) throws IOException{
 
        boolean createFileOrDir = FileUtils.createFileOrDir(dirURI, 
                Connector.READ_WRITE, true);
        if (!createFileOrDir){
            IOException e = new IOException();
            throw e;
        }
        return createFileOrDir;

    }
    
    public boolean removeRecursively(String dirURI){
        if (!dirURI.endsWith("/")){
            dirURI += "/";
        }
        try { 
            boolean success = FileUtils.deleteRecursively(dirURI, true);
            if (success){
                //Wanna do something?
            }else{
                IOException e = new IOException();
                throw e;
            }
            return success;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
        
    }

    public String getActiveUser() {
        //Code here:
        return AppPref.getSetting("CURRENTUSER");
    }

    public void setActiveUserAuth(String password) {
        AppPref.addSetting("CURRENTUSERAUTH", password);
    }

    public String getActiveUserAuth() {
        return AppPref.getSetting("CURRENTUSERAUTH");
    }

    public String getAppPref(String key) {       
        String value;
        value = AppPref.getSetting(key);
        return value;
    }

    /**
     * @inheritDoc
     */
    public HTTPResult makeRequest(final String url, final Hashtable headers, final Hashtable postParameters, final String m) {
        int meaning = 42;
        getLogger().l(UMLog.VERBOSE, 305, "HTTP (" + m + ")" + url);
        try {
            return HTTPUtils.makeHTTPRequest(url, postParameters, headers, m);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void setAppPref(String key, String value) {
        AppPref.addSetting(key, value);
    }

    public XmlPullParser newPullParser() throws XmlPullParserException { 
        KXmlParser parser = new KXmlParser();
        return parser;
    }

    public String getUserPref(String key) {
        String value = UserPref.getSetting(key);
        return value;
    }

    public AppView getAppView() {
        return appView;
    }

    public UMLog getLogger() {
        return umLogger;
    }

    public String openContainer(UstadJSOPDSEntry entry, String containerURI, 
            String mimeType) {
        return null;
    }

    public void closeContainer(String openURI) {
        //ToDo
    }

    public InputStream getFileInputStreamFromZip(String zipURI, String filename) {
        return null;
    }

    public String[] getUserPrefKeyList() {
        return UserPref.getAllKeys();
    }

    public String[] getAppPrefKeyList() {
        return AppPref.getAllKeys();
    }

    public ZipFileHandle openZip(String name) throws IOException {
        return null;
    }

    public OutputStream openFileOutputStream(String fileURI, boolean autocreate) throws IOException{
        FileConnection con = null;
        IOException e = null;
        if(autocreate) {
            try {
                con = (FileConnection)Connector.open(fileURI);
                if(!con.exists()) {
                    con.create();
                }
            }catch(IOException e2) {
                e = e2;
            }finally {
                J2MEIOUtils.closeConnection(con);
                if(e != null) throw e;
            }
        }
        
        OutputStream out = Connector.openOutputStream(fileURI);
        return out;
    }

    public String[] getPrefKeyList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * J2ME implementation of the DownloadJob interface 
     */
    public class DownloadJob extends Thread implements UMTransferJob {

        final private String srcURL;
        
        final private String destFileURI;
        
        private int bytesDownloaded;
        
        private int totalSize;
        
        private UstadMobileSystemImplJ2ME myImpl;
        
        private boolean finished;
        
        private final Vector progressListeners;
        
        private final UMProgressEvent evt;
        
        /**
         * The delay (in ms) minimum between progress updates; this is used to
         * ensure that we don't fire out too many updates
         */
        public static final int UPDATE_MIN_INTERVAL = 1000;
        
        /**
         * Create a new download job
         * 
         * @param srcURL The HTTP source URL to download from
         * @param destFileURI The file path to save to 
         * @param myImpl our parent SystemImplementation
         */
        public DownloadJob(String srcURL, String destFileURI, UstadMobileSystemImplJ2ME myImpl) {
            this.srcURL = srcURL;
            this.destFileURI = destFileURI;
            
            bytesDownloaded = -1;
            totalSize = -1;
            this.myImpl =  myImpl;
            finished = false;
            progressListeners = new Vector();
            evt = new UMProgressEvent(this, UMProgressEvent.TYPE_PROGRESS, 0, 0, 0);
        }
        
        /**
         * Send update event to all registered listeners
         */
        protected void fireProgressEvent() {
            int i;
            int numListeners = progressListeners.size();
            for(i = 0; i < numListeners; i++) {
                ((UMProgressListener)progressListeners.elementAt(i)).progressUpdated(evt);
            }
        }
        
        public void start() {
            super.start();
        }
        
        /**
         * Run as a thread the actual download (in the background)
         */
        public void run() {
            final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            OutputStream fOut = null;
            HttpConnection con = null;
            InputStream httpIn = null;
            try {
                if(totalSize == -1) {
                    getTotalSize();
                }
                
                //see if we need to delete the destination beforehand
                if(impl.fileExists(destFileURI)) {
                    impl.removeFile(destFileURI);
                }
                
                fOut = myImpl.openFileOutputStream(destFileURI, true);
                con = (HttpConnection)Connector.open(srcURL);
                httpIn = con.openInputStream();
                myImpl.getLogger().l(UMLog.VERBOSE, 312, srcURL);
                //UMIOUtils.readFully(httpIn, fOut, 1024);
                
                byte[] buf = new byte[1024];
                int bytesRead = 0;
                int totalRead = 0;
                long lastUpdate = 0;
                
                long timeNow;
                while((bytesRead = httpIn.read(buf)) != -1) {
                    fOut.write(buf, 0, bytesRead);
                    totalRead += bytesRead;
                    timeNow = System.currentTimeMillis();
                    if(timeNow - lastUpdate > UPDATE_MIN_INTERVAL) {
                        evt.setProgress(totalRead);
                        System.out.println("Firing progress evt: " + totalRead);
                        fireProgressEvent();
                        lastUpdate = timeNow;
                    }
                }
                
                
                
                finished = true;
                this.bytesDownloaded = totalSize;
                evt.setEvtType(UMProgressEvent.TYPE_COMPLETE);
                evt.setJobLength(totalSize);
                evt.setProgress(totalSize);
                evt.setStatusCode(200);
                fireProgressEvent();
            }catch(IOException e) {
                impl.getLogger().l(UMLog.INFO, 1, srcURL, e);
            }finally {
                UMIOUtils.closeInputStream(httpIn);
                J2MEIOUtils.closeConnection(con);
                UMIOUtils.closeOutputStream(fOut);
            }
        }

        /**
         * @inheritDoc
         */
        public void addProgressListener(UMProgressListener listener) {
            progressListeners.addElement(listener);
        }

        /**
         * @inheritDoc
         */
        public int getBytesDownloadedCount() {
            return bytesDownloaded;
        }

        /**
         * @inheritDoc
         */
        public int getTotalSize() {
            HttpConnection con = null;
            try {
                con = (HttpConnection)Connector.open(srcURL);
                con.setRequestMethod(HttpConnection.HEAD);
                String contentLen = con.getHeaderField("Content-Length");
                if(contentLen != null) {
                    totalSize = Integer.parseInt(contentLen);
                }
            }catch(Exception e) {
                UstadMobileSystemImpl.getInstance().getLogger().l(UMLog.INFO, 102, null, e);
            }finally {
                J2MEIOUtils.closeConnection(con);
                con = null;
            }
            
            return totalSize;
        }

        /**
         * @inheritDoc
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * @inheritDoc
         */
        public String getSource() {
            return srcURL;
        }

        /**
         * @inheritDoc
         */
        public String getDestination() {
            return destFileURI;
        }
        
    }
    

}
