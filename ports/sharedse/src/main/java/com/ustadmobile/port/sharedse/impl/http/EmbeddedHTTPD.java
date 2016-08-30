package com.ustadmobile.port.sharedse.impl.http;



import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.*;
import fi.iki.elonen.router.RouterNanoHTTPD;

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
public class EmbeddedHTTPD extends RouterNanoHTTPD {

    //private HashMap<String, MountedZip> mountedEPUBs;

    private int id;

    public static int idCounter = 0;

    public static final String PREFIX_MOUNT = "/mount/";

    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    @SuppressWarnings("rawtypes")
    private static HashMap<String, String> theMimeTypes = new HashMap<>();

    static
    {
        StringTokenizer st = new StringTokenizer(
                "css		text/css "+
                        "htm		text/html "+
                        "html		text/html "+
                        "xhtml		application/xhtml+xml "+
                        "xml		text/xml "+
                        "txt		text/plain "+
                        "asc		text/plain "+
                        "gif		image/gif "+
                        "jpg		image/jpeg "+
                        "jpeg		image/jpeg "+
                        "png		image/png "+
                        "mp3		audio/mpeg "+
                        "m3u		audio/mpeg-url " +
                        "mp4		video/mp4 " +
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
                        "class		application/octet-stream " );
        while ( st.hasMoreTokens())
            theMimeTypes.put( st.nextToken(), st.nextToken());
    }





    public EmbeddedHTTPD(int portNum) {
        super(portNum);
        //mountedEPUBs = new HashMap<>();
        id = idCounter;
        idCounter++;
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
    public String mountZip(String zipPath, String mountPath, HashMap<String, List<MountedZipHandler.MountedZipFilter>> filters) {
        if(mountPath == null) {
            mountPath= UMFileUtil.getFilename(zipPath) + '-' +
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }

        try {
            ZipFile zipFile = new ZipFile(zipPath);
            addRoute(PREFIX_MOUNT + mountPath + "/" + MountedZipHandler.URI_ROUTE_POSTFIX,
                    MountedZipHandler.class, zipFile, filters);
            return PREFIX_MOUNT + URLEncoder.encode(mountPath, "UTF-8");
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 90, zipPath, e);
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
            String route = PREFIX_MOUNT + URLDecoder.decode(encodedPath, "UTF-8") + "/" + MountedZipHandler.URI_ROUTE_POSTFIX;
            removeRoute(route);
        }catch(UnsupportedEncodingException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 20, mountPath, e);
        }

    }

}
