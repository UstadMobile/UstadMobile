package com.ustadmobile.port.sharedse.networkmanager;

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
import java.util.Properties;

/**
 * <h1>ResumableHttpDownload</h1>
 *
 * This class encapsulates HTTP resume logic, it is also responsible to download the file from
 * its source to our application.
 *
 * @author mike
 */
public class ResumableHttpDownload {

    private String httpSrc;

    private String destinationFile;

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

    public ResumableHttpDownload(String httpSrc, String destinationFile){
        this.httpSrc = httpSrc;
        this.destinationFile = destinationFile;
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
        HttpURLConnection con = null;
        URL url;
        OutputStream fileOut = null;
        OutputStream propertiesOut = null;
        InputStream propertiesIn = null;
        InputStream httpIn = null;

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
                con = (HttpURLConnection) url.openConnection();
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
                }
            }

            if(startFrom == 0 && dlPartFile.exists()) {
                //Part file exists but does not match the last modified and/or etag
                dlPartFile.delete();
            }

            con = (HttpURLConnection)url.openConnection();
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

            int responseCode = con.getResponseCode();
            boolean appendToPartFileOutput = responseCode == HttpURLConnection.HTTP_PARTIAL;
            fileOut = new BufferedOutputStream(new FileOutputStream(dlPartFile, appendToPartFileOutput));
            synchronized (this) {
                downloadedSoFar = appendToPartFileOutput ? dlPartFileSize : 0L;
            }

            byte[] buf = new byte[bufferSize];
            int bytesRead;
            httpIn = con.getInputStream();

            while(!isStopped() && (bytesRead = httpIn.read(buf)) != -1) {
                fileOut.write(buf, 0, bytesRead);
                synchronized (this) {
                    downloadedSoFar += bytesRead;
                }
            }
            fileOut.flush();

            completed = downloadedSoFar == totalSize;
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(propertiesIn);
            UMIOUtils.closeOutputStream(propertiesOut);
            UMIOUtils.closeInputStream(httpIn);
            UMIOUtils.closeOutputStream(fileOut);


            if(con != null) {
                con.disconnect();
            }
        }

        if(completed) {
            File destinationFile = new File(this.destinationFile);
            if(overwriteDestination && destinationFile.exists()){
                destinationFile.delete();
            }

            if(!dlPartFile.renameTo(destinationFile)) {
                throw new IllegalStateException("Unable to rename completed download part file to destination file");
            }

            dlInfoFile.delete();
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
     * The total size of the download if known in bytes,or -1 if that's not yet known
     *
     * @return long: Total bytes in a file
     */
    public long getTotalSize() {
        return totalSize;
    }

    public synchronized void stop() {
        stopped = true;
    }

    protected synchronized boolean isStopped() {
        return stopped;
    }


}
