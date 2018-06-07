package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * <h1>ResumableHttpDownload</h1>
 *
 * This class encapsulates HTTP resume logic, it is also responsible to download the file from
 * its source to our application.
 *
 * @author mike
 */
public class ResumableHttpDownload {

    private static final String SUBLOGTAG = "ResumableHttpDownload";

    private String httpSrc;

    private String destinationFile;

    private InputStream httpIn;

    private OutputStream fileOut;

    private HttpURLConnection con;

    /**
     * Extension of the file which carry file information
     */
    public static final String DLINFO_EXTENSION = ".dlinfo";

    /**
     * Extension of the partially downloaded file.
     */
    public static final String DLPART_EXTENSION = ".dlpart";

    private static final String HTTP_HEADER_LAST_MODIFIED ="last-modified";

    private static final String HTTP_HEADER_ETAG = "etag";

    private static final String HTTP_HEADER_CONTENT_RANGE = "content-range";

    /**
     * HTTP header accepted encoding type.
     */
    public static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * HTTP encoding identity
     */
    public static final String HTTP_ENCODING_IDENTITY = "identity";

    private int bufferSize = 8 *1024;//8KB

    private boolean overwriteDestination = true;

    private volatile long totalSize = -1;

    private volatile long downloadedSoFar;

    private boolean stopped = false;

    /**
     * The timeout to read data. The HttpUrlConnection client on Android by default seems to leave
     * this as being infinite
     */
    private static final int HTTP_READ_TIMEOUT = 5000;

    /**
     * The timeout to connect to an http server. The HttpUrlConnection client on Android by default
     * seems to leave this as being infinite
     */
    private static final int HTTP_CONNECT_TIMEOUT = 10000;

    private boolean keepAliveEnabled = true;

    //map of time (ms) to totalBytesDownloaded at that time to allow calculation of moving average of download speed
    private TreeMap<Long, Long> downloadProgressHistory = new TreeMap<>();

    private volatile long progressHistoryLastRecorded;

    private int progressHistoryInterval = 1000;

    private int progressHistoryStackSize = 5;

    private URLConnectionOpener connectionOpener;

    private final String logPrefix;

    private int responseCode;

    public ResumableHttpDownload(String httpSrc, String destinationFile){
        this.httpSrc = httpSrc;
        this.destinationFile = destinationFile;
        logPrefix = SUBLOGTAG + "(" + System.identityHashCode(this)+")";
    }

    /**
     * Method which download the file from its source.
     * @return boolean: TRUE, if the file was downloaded successfully otherwise FALSE.
     * @throws IOException
     */
    public boolean download() throws IOException{
        File dlInfoFile = new File(destinationFile + DLINFO_EXTENSION);
        File dlPartFile = new File(destinationFile + DLPART_EXTENSION);

        IOException ioe = null;
        Properties dlInfoProperties = new Properties();
        URL url;
        OutputStream propertiesOut = null;
        InputStream propertiesIn = null;


        boolean completed = false;
        try {
            long startFrom = 0L;
            boolean dlPartFileExists = dlPartFile.exists();
            long dlPartFileSize = dlPartFileExists ? dlPartFile.length() : 0L;
            url = new URL(httpSrc);

            if(dlPartFile.exists() && dlInfoFile.exists()) {
                propertiesIn = new FileInputStream(dlInfoFile);
                dlInfoProperties.load(propertiesIn);
                propertiesIn.close();
                propertiesIn = null;
            }

            /**
             * To resume we must have the part download itself, it must have > 0 bytes to be useful,
             * and we must have a last modified date and/or etag to validate the download against
             */
            if(dlPartFileExists && dlPartFileSize > 0 && (dlInfoProperties.containsKey(HTTP_HEADER_LAST_MODIFIED)
                    || dlInfoProperties.containsKey(HTTP_HEADER_ETAG))) {

                if(connectionOpener != null) {
                    con = (HttpURLConnection)connectionOpener.openConnection(url);
                }else {
                    con = (HttpURLConnection) url.openConnection();
                }

                con.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
                con.setReadTimeout(HTTP_READ_TIMEOUT);
                con.setRequestMethod("HEAD");
                con.setRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_IDENTITY);

                con.connect();
                String serverLastModified = con.getHeaderField(HTTP_HEADER_LAST_MODIFIED);
                String serverEtag = con.getHeaderField(HTTP_HEADER_ETAG);
                httpIn = con.getInputStream();

                httpIn.close();
                con.disconnect();

                boolean etagInfoPresent = serverEtag != null && dlInfoProperties.containsKey(HTTP_HEADER_ETAG);
                boolean lastModifiedInfoPresent = serverLastModified != null
                        && dlInfoProperties.containsKey(HTTP_HEADER_LAST_MODIFIED);
                boolean validated = etagInfoPresent || lastModifiedInfoPresent;

                if(lastModifiedInfoPresent) {
                    validated &= serverLastModified.equals(dlInfoProperties.getProperty(HTTP_HEADER_LAST_MODIFIED));
                }

                if(etagInfoPresent) {
                    validated &= serverEtag.equals(dlInfoProperties.getProperty(HTTP_HEADER_ETAG));
                }

                if(validated) {
                    startFrom = dlPartFile.length();
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + " validated to start from " + startFrom);
                }else {
                    UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + " file exists but not validated");
                }
            }

            if(startFrom == 0 && dlPartFile.exists()) {
                //Part file exists but does not match the last modified and/or etag
                UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + " : startFrom = 0 and dlpart file exists");
                dlPartFile.delete();
            }

            if(connectionOpener != null) {
                con = (HttpURLConnection)connectionOpener.openConnection(url);
            }else {
                con = (HttpURLConnection) url.openConnection();
            }

            con.setRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_IDENTITY);
            con.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            con.setReadTimeout(HTTP_READ_TIMEOUT);

            if(startFrom > 0){
                con.setRequestProperty("Range", "bytes=" + String.valueOf(dlPartFileSize) + '-');
            }
            con.connect();

            //make the dlinfo properties file
            dlInfoProperties.clear();
            if(con.getHeaderField(HTTP_HEADER_ETAG) != null)
                dlInfoProperties.setProperty(HTTP_HEADER_ETAG, con.getHeaderField(HTTP_HEADER_ETAG));

            if(con.getHeaderField(HTTP_HEADER_LAST_MODIFIED) != null)
                dlInfoProperties.setProperty(HTTP_HEADER_LAST_MODIFIED,
                con.getHeaderField(HTTP_HEADER_LAST_MODIFIED));

            String contentRangeResponse = con.getHeaderField(HTTP_HEADER_CONTENT_RANGE);

            synchronized (this) {
                if(contentRangeResponse != null && con.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    contentRangeResponse = contentRangeResponse.substring(contentRangeResponse.indexOf('/')+1).trim();
                    if(!contentRangeResponse.equals("*")) {
                        totalSize = Long.parseLong(contentRangeResponse);
                    }
                }else {
                    totalSize = con.getContentLength();
                }

            }

            propertiesOut = new FileOutputStream(dlInfoFile);
            dlInfoProperties.store(propertiesOut, "UTF-8");
            propertiesOut.close();
            propertiesOut = null;

            responseCode = con.getResponseCode();
            boolean appendToPartFileOutput = responseCode == HttpURLConnection.HTTP_PARTIAL;
            synchronized (this) {
                if(isStopped()) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, mkLogPrefix() + " stopped before output file is to be opened");
                    return false;
                }

                fileOut = new BufferedOutputStream(new FileOutputStream(dlPartFile, appendToPartFileOutput));
                downloadedSoFar = appendToPartFileOutput ? dlPartFileSize : 0L;
                progressHistoryLastRecorded = System.currentTimeMillis();
            }

            synchronized (downloadProgressHistory){
                downloadProgressHistory.put(progressHistoryLastRecorded, downloadedSoFar);
            }

            byte[] buf = new byte[bufferSize];
            int bytesRead;
            httpIn = con.getInputStream();

            long currentTime;
            while((bytesRead = httpIn.read(buf)) != -1) {
                synchronized (this) {
                    if(!isStopped()) {
                        fileOut.write(buf, 0, bytesRead);
                    }else{
                        break;
                    }
                }


                currentTime = System.currentTimeMillis();
                synchronized (this) {
                    downloadedSoFar += bytesRead;
                }

                synchronized (downloadProgressHistory){
                    if((System.currentTimeMillis() - progressHistoryLastRecorded)
                            > progressHistoryInterval) {
                        downloadProgressHistory.put(currentTime, downloadedSoFar);
                        progressHistoryLastRecorded = currentTime;

                        if(downloadProgressHistory.size() > progressHistoryStackSize)
                            downloadProgressHistory.remove(downloadProgressHistory.firstKey());
                    }
                }
            }

            synchronized (this){
                if(!isStopped()) {
                    fileOut.flush();
                }
                completed = downloadedSoFar == totalSize;
            }
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(propertiesIn);
            UMIOUtils.closeOutputStream(propertiesOut);
            UMIOUtils.closeInputStream(httpIn);
            httpIn = null;
            synchronized (this) {
                UMIOUtils.closeOutputStream(fileOut);
                fileOut = null;
            }

            if(con != null) {
                con.disconnect();
                con = null;
            }
        }

        if(completed) {
            UstadMobileSystemImpl.l(UMLog.INFO, 0, mkLogPrefix() + " completed, downloaded " +
                    downloadedSoFar + "bytes");
            synchronized (this) {
                File destinationFile = new File(this.destinationFile);
                if(overwriteDestination && destinationFile.exists()){
                    UstadMobileSystemImpl.l(UMLog.INFO, 0, mkLogPrefix() +
                            " download complete, overwrite enabled, deleting existing file" +
                            destinationFile.getAbsolutePath());
                    destinationFile.delete();
                }

                if(dlPartFile.renameTo(destinationFile)) {
                    UstadMobileSystemImpl.l(UMLog.INFO, 0,
                            mkLogPrefix() + " download completed, moved " +
                            dlInfoFile.getAbsolutePath() + " to " + destinationFile.getAbsolutePath());
                }else {
                    throw new IllegalStateException(
                            mkLogPrefix() + "Unable to rename completed download part file to destination file from"
                                    + dlPartFile.getAbsolutePath() + " to " + destinationFile.getAbsolutePath());
                }

                dlInfoFile.delete();
            }
        }

        UMIOUtils.throwIfNotNullIO(ioe);

        return completed;
    }

    /**
     * The total in bytes downloaded so far. Includes 'resumed' bytes from previous attempts
     *
     * @return long: Total bytes downloaded
     */
    public long getDownloadedSoFar() {
        return downloadedSoFar;
    }

    /**
     * Calculate the download speed in bytes per second.
     *
     * @return The current download speed in btyes per second
     */
    public long getCurrentDownloadSpeed() {
        Map.Entry<Long, Long> firstEntry;
        Map.Entry<Long, Long> lastEntry;

        synchronized (downloadProgressHistory) {
            if(downloadProgressHistory.size() < 2)
                return 0;

            firstEntry = downloadProgressHistory.firstEntry();
            lastEntry = downloadProgressHistory.lastEntry();
        }

        //divide delta in byte download by delta time (ms), multiply by 1000 to get speed in bytes per second
        return  ((lastEntry.getValue() - firstEntry.getValue())
                    / (lastEntry.getKey() - firstEntry.getKey())) * 1000;
    }


    /**
     * The minimum amount of time (in ms) after which the progress of a download will be checked and
     * recorded, for use calculating the speed. This is checked during download after filling the
     * buffer. 1000ms by default.
     *
     * @return Interval period for measurement of download progress (in ms)
     */
    public int getProgressHistoryInterval() {
        return progressHistoryInterval;
    }

    /**
     * Setter for progressHistoryInterval
     *
     * @see #getProgressHistoryInterval()
     *
     * @param progressHistoryInterval Interval period for measurement of download progress (in ms)
     */
    public void setProgressHistoryInterval(int progressHistoryInterval) {
        this.progressHistoryInterval = progressHistoryInterval;
    }

    /**
     * Calculate the download speed for presentation to the user we use a moving average. This
     * is a First In First Out stack. Essentially the download speed returns the average download
     * speed over (stackSize x historyInterval) ms.
     *
     * @return The stack size for the download progress history tracking. Default 5
     */
    public int getProgressHistoryStackSize() {
        return progressHistoryStackSize;
    }

    /**
     * Setter for progressHistoryStackSize
     *
     * @see #getProgressHistoryStackSize()
     *
     * @param progressHistoryStackSize The stack size for the download progress history tracking.
     */
    public void setProgressHistoryStackSize(int progressHistoryStackSize) {
        this.progressHistoryStackSize = progressHistoryStackSize;
    }

    /**
     * The total size of the download if known in bytes,or -1 if that's not yet known
     *
     * @return long: Total bytes in a file
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Stop this download. Everything downloaded so far will be written to disk, and the file will
     * be closed. This method is thread safe, and no reading or writing of the output file will
     * take place after this method returns.
     *
     * @return the number of bytes downloaded
     */
    public synchronized long stop() {
        stopped = true;
        UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + " stop() called");

        //close the file output stream
        if(fileOut != null) {
            try {
                fileOut.flush();
                UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + "stop: flushed fileout OK");
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 0, mkLogPrefix() + "stop: exception flushing fileOut", e);
            }

            try {
                fileOut.close();
                UstadMobileSystemImpl.l(UMLog.DEBUG, 0, mkLogPrefix() + "stop: closed fileout OK");
            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 0, mkLogPrefix() + "stop: exception closing fileout", e);
            }finally {
                fileOut = null;
            }
        }

        return downloadedSoFar;
    }

    protected synchronized boolean isStopped() {
        return stopped;
    }

    public URLConnectionOpener getConnectionOpener() {
        return connectionOpener;
    }

    public void setConnectionOpener(URLConnectionOpener connectionOpener) {
        this.connectionOpener = connectionOpener;
    }

    private final String mkLogPrefix(){
        return logPrefix;
    }

    /**
     * Get the http response code (if the response has started)
     *
     * @return HTTP response code (if the ressponse has started)
     */
    public int getResponseCode() {
        return responseCode;
    }
}
