package com.ustadmobile.port.sharedse.impl.http;


import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManager.CATALOG_HTTP_ENDPOINT_PREFIX;

/**
 * Embedded HTTP Server which runs to serve files directly out of a zipped container on the fly
 *
 * Mounted zips will be acessible under http://IP:PORT/mount/mountName
 *
 * For performance reasons mounted zip files are served with cache headers with a max-age to prevent
 * additional requests - therefor the mountName should include a date or timestamp component to prevent
 * stale files being served.
 *
 * Created by mike on 8/14/15.
 */
public class EmbeddedHTTPD extends RouterNanoHTTPD implements ResponseMonitoredInputStream.OnCloseListener{

    //private HashMap<String, MountedZip> mountedEPUBs;

    private int id;

    public static int idCounter = 0;

    public static final String PREFIX_MOUNT = "/mount/";

    public interface ResponseListener {

        void responseStarted(NanoHTTPD.Response response);

        void responseFinished(NanoHTTPD.Response response);

    }

    private Vector<ResponseListener> responseListeners = new Vector<>();

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    @SuppressWarnings("rawtypes")
    private static HashMap<String, String> theMimeTypes = new HashMap<>();

    private Hashtable<String, ZipFile> mountedZips = new Hashtable<>();

    static
    {
        theMimeTypes.put("htm", "text/html");
        theMimeTypes.put("html", "text/html");
        theMimeTypes.put("xhtml", "application/xhtml+xml");
        theMimeTypes.put("xml", "text/xml");
        theMimeTypes.put("txt", "text/plain");

        StringTokenizer st = new StringTokenizer(
                "css		text/css "+
                        "asc		text/plain "+
                        "gif		image/gif "+
                        "jpg		image/jpeg "+
                        "jpeg		image/jpeg "+
                        "png		image/png "+
                        "mp3		audio/mpeg "+
                        "m3u		audio/mpeg-url " +
                        "mp4		video/mp4 " +
                        "m4v        video/mp4 " +
                        "ogv		video/ogg " +
                        "flv		video/x-flv " +
                        "mov		video/quicktime " +
                        "swf		application/x-shockwave-flash " +
                        "js			application/javascript "+
                        "pdf		application/pdf "+
                        "doc		application/msword "+
                        "ogg		application/x-ogg "+
                        "zip		application/octet-stream "+
                        "exe		application/octet-stream "+
                        "wav		audio/wav "+
                        "class		application/octet-stream " +
                        "docx       application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        while ( st.hasMoreTokens())
            theMimeTypes.put( st.nextToken(), st.nextToken());
    }





    public EmbeddedHTTPD(int portNum, Object context) {
        super(portNum);
        //mountedEPUBs = new HashMap<>();
        id = idCounter;
        idCounter++;

        addRoute(CATALOG_HTTP_ENDPOINT_PREFIX + "(.)+", CatalogUriResponder.class, context,
                new WeakHashMap(), this);
        //TODO: Setup 404 handling
    }

    @Override
    public void addMappings() {
        super.addMappings();
    }


    public String toString() {
        return "EmbeddedHTTPServer on port : " + getListeningPort() + " id: " + id;
    }

    public static String getMimeType(String uri) {
        String mimeResult = theMimeTypes.get(UMFileUtil.getExtension(uri));
        return mimeResult != null ? mimeResult : "application/octet-stream";
    }


    /**
     * Mount a zip to the given path.  The contents of the zip file will then be accessible by
     * HTTP using http://IP:PORT/mount/mountPath
     *
     * Zips should be unmounted when they are no longer needed.  Depending on how Android feels
     * this service may live on after an activity is finished.  The mounted zip keeps a cached
     * copy of the ZipFile object containing entry names, file sizes, data positions etc.
     *
     * For performance the mountPath should include a time/date component.  All files served will be
     * with cache a 1 year maxage cache header
     *
     * @param mountPath The path to use after /mount .
     * @param zipPath The local filesystem path to the zip file (e.g. /path/to/file.epub)
     */
    public String mountZip(String zipPath, String mountPath, boolean epubHtmlFilterEnabled,
                           String epubScriptPath) {
        if(mountPath == null) {
            mountPath= UMFileUtil.getFilename(zipPath) + '-' +
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }

        try {
            ZipFile zipFile = new ZipFile(zipPath);
            addRoute(PREFIX_MOUNT + mountPath + "/" + MountedZipHandler.URI_ROUTE_POSTFIX,
                    MountedZipHandler.class, zipFile, epubHtmlFilterEnabled, epubScriptPath);
            String fullPath = toFullZipMountPath(mountPath);
            mountedZips.put(fullPath, zipFile);
            return toFullZipMountPath(mountPath);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 90, zipPath, e);
        }

        return null;
    }

    private String toFullZipMountPath(String mountPath) {
        try {
            return PREFIX_MOUNT + URLEncoder.encode(mountPath, "UTF-8");
        }catch(UnsupportedEncodingException e){
            //Should enver happen
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, null, e);
        }

        return null;
    }

    /**
     * Unmount a zip that was mounted with mountZip
     *
     * @param mountPath The mount path given to mount the zip
     */
    public void unmountZip(String mountPath) {
        String encodedPath = mountPath.substring(PREFIX_MOUNT.length());
        try {
            String route = PREFIX_MOUNT + URLDecoder.decode(encodedPath, "UTF-8") + "/"
                    + MountedZipHandler.URI_ROUTE_POSTFIX;
            removeRoute(route);
            mountedZips.remove(toFullZipMountPath(mountPath));
        }catch(UnsupportedEncodingException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 20, mountPath, e);
        }
    }

    /**
     * Convenience method to make the ZipFile object accessible if a presenter needs it after
     * mounting it on http. This will avoid having to read the file again.
     *
     * @param mountPath The path as returned by mountZip
     *
     * @return ZipFile object for the zip that was mounted on that path, null if it's not mounted.
     */
    public ZipFile getMountedZip(String mountPath){
        return mountedZips.get(mountPath);
    }

    /**
     * Returns the local URL in the form of http://localhost;PORT/
     *
     * @return Local URL as above including a trailing slash
     */
    public String getLocalURL() {
        return "http://localhost:" + getListeningPort() + "/";
    }

    /**
     * Add an entry response listener. This will receive response events when entries are sent to
     * clients.
     *
     * @param listener
     */
    public void addResponseListener(ResponseListener listener) {
        responseListeners.add(listener);
    }

    /**
     * Remove an entry response listener.
     *
     * @param listener
     */
    public void removeResponseListener(ResponseListener listener) {
        responseListeners.remove(listener);
    }

    protected void fireResponseStarted(NanoHTTPD.Response response) {
        synchronized (responseListeners) {
            for(ResponseListener listener : responseListeners) {
                listener.responseStarted(response);
            }
        }
    }

    protected void fireResponseFinished(NanoHTTPD.Response response) {
        synchronized (responseListeners) {
            for(ResponseListener listener: responseListeners) {
                listener.responseFinished(response);
            }
        }
    }

    /**
     * Called when a response has started. Because NanoHTTPD's router will create a new
     * CatalogUriResponder for each response, we provide this EmbeddedHTTP instance as a parameter to
     * the responder, which in turn calls this method.
     *
     * @param response
     */
    protected void handleResponseStarted(NanoHTTPD.Response response) {
        fireResponseStarted(response);
    }

    @Override
    public void onStreamClosed(NanoHTTPD.Response response) {
        fireResponseFinished(response);
    }
}
