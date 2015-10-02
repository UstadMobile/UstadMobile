/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.impl;

import com.ustadmobile.core.impl.UMDownloadCompleteEvent;
import com.ustadmobile.core.impl.UMDownloadCompleteReceiver;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.j2me.app.SerializedHashtable;
import com.ustadmobile.port.j2me.util.J2MEIOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author mike
 */
public class DownloadServiceJ2ME implements Runnable {
    
    private boolean running;
    
    private static final String RMSNAME = "UMJ2MEDownloads";
    
    private Hashtable statusTracker;
    
    private Thread serviceThread;
    
    private static String KEY_DOWNLOADQ = "dlq";
    
    private static String KEY_NEXTID = "nid";
    
    private static final String PREFIXJOB = "j-";
    
    private static final String POSTFIX_SRCURL = "-srcURL";
    
    private static final String POSTFIX_DESTFILEURI = "-destFileURI";
    
    private static final String POSTFIX_HEADERS = "-headers";
    
    private static final String POSTFIX_METHOD = "-method";
    
    private static final String POSTFIX_BYTECOUNT = "-b";
    
    private static final String POSTFIX_TOTALBYTES = "-t";
    
    private static final String POSTFIX_STATUS = "-s";
    
    private Vector downloadStatusReceivers;
    
    /**
     * If the key for this field in the status tables ends with any of these
     * postfixes we should parse it into an integer.  Saving serialized hashtable
     * always converts values into strings.  They therefor need converted bcak
     * on loading.
     */
    private static final String[] POSTFIX_INTEGERS = new String[] { 
        POSTFIX_BYTECOUNT, POSTFIX_TOTALBYTES, POSTFIX_STATUS};

    
    /**
     * The time the main thread will wait before checking for the next download.
     */
    public static final int SLEEP_INTERVAL = 1000;
    
    /**
     * The time to pause after an error with a download before retrying
     */
    public static final int PAUSE_INTERVAL = 20000;
    
    public static final int DLBUFSIZE = 2048;
    
    private final Object qLock = new Object();
    
    public DownloadServiceJ2ME() {
        downloadStatusReceivers = new Vector();
    }
    
    /**
     * Add the given download request to the queue.  If there are already downloads
     * running this download will run after those have finished.  The queue 
     * runs downloads sequentially in the order in which they were queued
     * 
     * @param request Download request to run
     * @return An ID uniquely identifying that download
     */
    public long enqueue(DownloadRequest request) {
        long nextId = Long.parseLong((String)statusTracker.get(KEY_NEXTID));
        statusTracker.put(KEY_NEXTID, String.valueOf(nextId + 1));
        
        JSONArray queueArr  = getQueue();
        queueArr.put(nextId);
        setQueue(queueArr);
        
        statusTracker.put(nextId + POSTFIX_SRCURL, request.srcURL);
        statusTracker.put(nextId + POSTFIX_DESTFILEURI, request.destFileURI);
        JSONObject headersObj = new JSONObject(request.headers);
        statusTracker.put(nextId + POSTFIX_HEADERS, headersObj.toString());
        statusTracker.put(nextId + POSTFIX_METHOD, request.method);
        statusTracker.put(nextId + POSTFIX_STATUS, 
            new Integer(UstadMobileSystemImpl.DLSTATUS_PENDING));
        statusTracker.put(nextId + POSTFIX_BYTECOUNT, new Integer(0));
        statusTracker.put(nextId + POSTFIX_TOTALBYTES, new Integer(-1));
        save();
        
        //start the serviceThread if it's not running.  This has no effect if it is already going
        start();
        return nextId;
    }
    
    /**
     * Get the current status of a given download by it's ID
     * 
     * @param downloadID The download ID in question
     * 
     * @return An array of type int with the current byte count, total size and status int
     * 
     * @see UstadMobileSystemImpl#IDX_DOWNLOADED_SO_FAR
     * @see UstadMobileSystemImpl#IDX_BYTES_TOTAL
     * @see UstadMobileSystemImpl#IDX_STATUS
     */
    public int[] getStatus(long downloadID) {
        String fieldName = downloadID + POSTFIX_STATUS;
        if(statusTracker.containsKey(fieldName)) {
            int dlStatus = ((Integer)statusTracker.get(fieldName)).intValue();
            int dlTotalSize = ((Integer)statusTracker.get(downloadID + 
                    POSTFIX_TOTALBYTES)).intValue();
            int dlByteCount = ((Integer)statusTracker.get(downloadID + 
                    POSTFIX_BYTECOUNT)).intValue();
            return new int[] {dlByteCount, dlTotalSize, dlStatus};
        }else {
            return null;
        }
    }
    
    protected JSONArray getQueue() {
        JSONArray queueArr = null;
        if(statusTracker.get(KEY_DOWNLOADQ) != null) {
            try {
                queueArr = new JSONArray((String)statusTracker.get(KEY_DOWNLOADQ));
            }catch(JSONException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 171, (String)statusTracker.get(KEY_DOWNLOADQ), e);
            }
        }
        
        if(queueArr == null) {
            queueArr = new JSONArray();
        }
        
        return queueArr;
    }
    
    public void registerDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver) {
        downloadStatusReceivers.addElement(receiver);
    }
   
    public void unregisterDownloadCompleteReceiver(UMDownloadCompleteReceiver receiver) {
        downloadStatusReceivers.removeElement(receiver);
    }
    
    
    protected void setQueue(JSONArray queue) {
        statusTracker.put(KEY_DOWNLOADQ, queue.toString());
    }
    
    /**
     * Save the status of download jobs into the record store.
     */
    public void save() {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(RMSNAME, true);
            byte[] recordBytes = 
                    SerializedHashtable.hashTabletoStream(statusTracker);
            if(store.getNumRecords() > 0) {
                RecordEnumeration e =store.enumerateRecords(null, null, false);
                int recordID = e.nextRecordId();
                store.setRecord(recordID, recordBytes, 0, recordBytes.length);
            }else {
                store.addRecord(recordBytes, 0, recordBytes.length);
            }
        }catch(RecordStoreException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 167, RMSNAME, e);
        }finally {
            J2MEIOUtils.closeRecordStore(store);
        }
    }
    
    /**
     * Load the status of download jobs from the record store
     */
    public void load() {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(RMSNAME, true);
            if(store.getNumRecords() > 0) {
                RecordEnumeration e =store.enumerateRecords(null, null, false);
                int recordID = e.nextRecordId();
                byte[] htBytes = store.getRecord(recordID);
                statusTracker = SerializedHashtable.streamToHashtable(htBytes);
            }
        }catch(RecordStoreException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 171, RMSNAME, e);
        }finally {
            J2MEIOUtils.closeRecordStore(store);
        }
        
        if(statusTracker == null) {
            statusTracker = new Hashtable();
        }
        
        Enumeration keys = statusTracker.keys();
        String currentKey;
        int i;
        while(keys.hasMoreElements()) {
            currentKey = (String)keys.nextElement();
            for(i = 0; i < POSTFIX_INTEGERS.length; i++) {
                if(currentKey.endsWith(POSTFIX_INTEGERS[i])) {
                    statusTracker.put(currentKey, Integer.valueOf(
                        statusTracker.get(currentKey).toString()));
                    break;
                }
            }
        }
        
        if(statusTracker.get(KEY_NEXTID) == null) {
            statusTracker.put(KEY_NEXTID, "0");
        }
    }
    
    /**
     * Starts the thread which will check for jobs
     */
    public void start() {
        if(serviceThread == null) {
            setRunning(true);
            serviceThread = new Thread(this);
            serviceThread.start();
        }
    }
    
    /**
     * Stops the thread which will check for jobs.
     */
    public void stop() {
        setRunning(false);
        serviceThread = null;
    }
    
    public static class DownloadRequest {
        
        private String srcURL;
        
        private String destFileURI;
        
        private Hashtable headers;
        
        private String method;
        
        public DownloadRequest(String srcURL, String destFileURI, Hashtable headers, String method) {
            this.srcURL = srcURL;
            this.destFileURI= destFileURI;
            this.headers = headers;
            this.method = method;
        }
        
        
    }
    
    public synchronized boolean isRunning() {
        return running;
    }
    
    public synchronized void setRunning(boolean running) {
        this.running = running;
    }
    
    public void run() {
        while(isRunning()) {
            JSONArray queue = getQueue();
            if(queue.length() > 0) {
                long downloadID = queue.optLong(0, -1);
                int jobStatus = continueDownload(downloadID);
                statusTracker.put(downloadID + POSTFIX_STATUS, 
                        new Integer(jobStatus));
                
                //remove this job from the queue
                if(jobStatus == UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL) {
                    //todo: remove info about this download from the hashtable.
                    //tell receivers about that news..
                    UMDownloadCompleteEvent evt = 
                        new UMDownloadCompleteEvent(downloadID, getStatus(downloadID));
                    for(int i = 0; i < downloadStatusReceivers.size(); i++) {
                        ((UMDownloadCompleteReceiver)downloadStatusReceivers.elementAt(i)).downloadStatusUpdated(evt);
                    }


                    Vector newQueue = new Vector();
                    synchronized(qLock) {
                        queue = getQueue();
                        for(int i = 1; i < queue.length(); i++) {
                            newQueue.addElement(queue.opt(i));
                        }

                        setQueue(new JSONArray(newQueue));
                    }

                    if(newQueue.size() == 0) {
                        stop();
                    }
                }
                //TODO: Handle counting fails 
            }
            
            try { Thread.sleep(SLEEP_INTERVAL); }
            catch(InterruptedException e) {}
        }
    }
    
    /**
     * Continue the given download in progress.  
     * 
     * @param downloadID DownloadID in question to continue
     * 
     * @return UstadMobileSystemImpl download status flag.
     */
    protected int continueDownload(long downloadID) {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        OutputStream fOut = null;
        HttpConnection con = null;
        InputStream httpIn = null;
        IOException ioe = null;

        String srcURL = (String)statusTracker.get(downloadID + POSTFIX_SRCURL);
        String destFileURI = (String)statusTracker.get(downloadID + POSTFIX_DESTFILEURI);
        
        boolean completed = false;
        int totalBytesKnown = ((Integer)statusTracker.get(
                downloadID + POSTFIX_TOTALBYTES)).intValue();
        
        
        try {
            impl.l(UMLog.INFO, 332, srcURL + "->" + destFileURI);
            
            //the bytes already downloaded (e.g. from previous download in case of resuming
            int bytesDownloaded = 0;
            if(impl.fileExists(destFileURI)) {
                bytesDownloaded = (int)impl.fileSize(destFileURI);
            }


            fOut = impl.openFileOutputStream(destFileURI, UstadMobileSystemImpl.FILE_APPEND);
            con = (HttpConnection)Connector.open(srcURL);
            if(bytesDownloaded > 0) {
                impl.l(UMLog.VERBOSE, 410, srcURL + ':' + bytesDownloaded);
                con.setRequestProperty("Range", "bytes=" + bytesDownloaded + '-');
            }
            con.setRequestProperty("Connection", "close");

            httpIn = con.openInputStream();
            String contentLenStr = con.getHeaderField("Content-Length");
            if(contentLenStr != null) {
                int totalBytes = Integer.parseInt(contentLenStr);
                if(totalBytesKnown == -1) {
                    //TODO: What would content-length be in the event of a partial request?
                    totalBytesKnown = totalBytes;
                    statusTracker.put(downloadID + POSTFIX_TOTALBYTES, 
                            new Integer(totalBytes));
                }else if(totalBytesKnown != totalBytes) {
                    //ouch - the file changed midway through
                    //TODO: FAIL HERE
                }
            }
            
            impl.getLogger().l(UMLog.VERBOSE, 314, srcURL);

            byte[] buf = new byte[DLBUFSIZE];
            int bytesRead = 0;
            int totalRead = 0;

            final String byteCountKey = downloadID + POSTFIX_BYTECOUNT;
            
            while((bytesRead = httpIn.read(buf)) != -1 && isRunning()) {
                fOut.write(buf, 0, bytesRead);
                totalRead += bytesRead;
                statusTracker.put(byteCountKey, 
                        new Integer(totalRead + bytesDownloaded));
            }

            completed = true;
            StringBuffer sbMsg = new StringBuffer();
            sbMsg.append(srcURL).append("->").append(destFileURI).append(" (");
            sbMsg.append(totalBytesKnown).append(" bytes)");
            impl.getLogger().l(UMLog.INFO, 333, sbMsg.toString());
            //fireProgressEvent();
        }catch(IOException e) {
            ioe = e;
            impl.l(UMLog.ERROR, 115, srcURL + "->" +  destFileURI, e);
        }finally {
            UMIOUtils.closeInputStream(httpIn);
            J2MEIOUtils.closeConnection(con);
            UMIOUtils.closeOutputStream(fOut);
        }

        if(completed) {
            return UstadMobileSystemImpl.DLSTATUS_SUCCESSFUL;
        }else {
            return UstadMobileSystemImpl.DLSTATUS_PAUSED;
        }
        
    }
}
