package com.ustadmobile.port.sharedse.impl.http;



import com.ustadmobile.core.util.UMFileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.*;

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
public class EmbeddedHTTPD extends NanoHTTPD {

    private HashMap<String, MountedZip> mountedEPUBs;

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
        mountedEPUBs = new HashMap<>();
        id = idCounter;
        idCounter++;
    }

    public String toString() {
        return "EmbeddedHTTPServer on port : " + getListeningPort() + " id: " + id;
    }

    public String getMimeType(String uri) {
        String mimeResult = theMimeTypes.get(UMFileUtil.getExtension(uri));
        return mimeResult != null ? mimeResult : "application/octet-stream";
    }

    private int[] parseRangeRequest(IHTTPSession session, int totalLength) {
        int[] range = null;
        String header = session.getHeaders().get("range");
        if(header != null  && header.startsWith("bytes=")) {
            range = new int[] {0, -1};
            header = header.substring("bytes=".length());
            int dashPos = header.indexOf('-');
            try {
                if(dashPos > 0) {
                    range[0] = Integer.parseInt(header.substring(0,dashPos));
                }

                if(dashPos == header.length()-1) {
                    range[1] = totalLength-1;
                }else if(dashPos > 0) {
                    range[1] = Integer.parseInt(header.substring(dashPos+1));

                }
            }catch(NumberFormatException nfe) {

            }
            if(range[0] < 0 || range[1] > totalLength) {
                range[0] = -1;//Error flag
            }
        }

        return range;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Response response = null;
        int range[];
        String ifNoneMatchHeader;
        InputStream retInputStream;

        if(uri.startsWith(PREFIX_MOUNT)) {
            int nextSlash = uri.indexOf('/', PREFIX_MOUNT.length() + 1);

            //check that a sub path is contained; if not return 404
            if(nextSlash == -1) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: only zip is mentioned, not path within:" + uri);
            }
            String zipMountPath = uri.substring(PREFIX_MOUNT.length(), nextSlash);

            if(!mountedEPUBs.containsKey(zipMountPath)) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: that zip is not mounted: " + uri);
            }

            String pathInZip = uri.substring(nextSlash + 1);
            try {
                MountedZip mountedZip = mountedEPUBs.get(zipMountPath);
                ZipFile zipFile = mountedZip.getZipFile();
                ZipEntry entry = zipFile.getEntry(pathInZip);

                if(entry != null) {
                    int totalLength = (int)entry.getSize();
                    String etag = Integer.toHexString((zipMountPath + pathInZip + entry.getTime() + "" +
                            totalLength).hashCode());
                    String extension = UMFileUtil.getExtension(pathInZip);
                    InputStream zipEntryStream = zipFile.getInputStream(entry);
                    retInputStream = zipEntryStream;

                    ifNoneMatchHeader = session.getHeaders().get("if-none-match");
                    if(ifNoneMatchHeader != null && ifNoneMatchHeader.equals(etag)) {
                        Response r = newFixedLengthResponse(Response.Status.NOT_MODIFIED, getMimeType(uri),
                                null);
                        r.addHeader("ETag", etag);
                        r.addHeader("Connection", "close");
                        return r;
                    }
                    

                    range = parseRangeRequest(session, totalLength);

                    //see if this is an entry we need to filter.
                    if(extension != null) {
                        if(mountedZip.hasFilter(extension)) {
                            byte[] buf = new byte[1024];
                            int bytesRead = -1;
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            while((bytesRead = zipEntryStream.read(buf, 0, buf.length)) != -1) {
                                bout.write(buf, 0, bytesRead);
                            }
                            zipEntryStream.close();
                            String contentStr = new String(bout.toByteArray(), "UTF-8");
                            contentStr = mountedZip.filterEntry(extension, contentStr);
                            byte[] strBytes = contentStr.getBytes("UTF-8");
                            retInputStream = new ByteArrayInputStream(strBytes);
                            totalLength = strBytes.length;
                        }
                    }

                    if(range != null) {
                        if(range[0] != -1) {
                            retInputStream = new RangeInputStream(retInputStream, range[0], range[1]);
                            int contentLength = (range[1]+1) - range[0];
                            Response r = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, getMimeType(uri),
                                retInputStream, contentLength);

                            r.addHeader("ETag", etag);

                            /*
                             * range request is inclusive: e.g. range 0-1 length is 2 bytes as per
                             * https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html 14.35.1 Byte Ranges
                             */
                            r.addHeader("Content-Length", String.valueOf(contentLength));
                            r.addHeader("Content-Range", "bytes " + range[0] + '-' + range[1] +
                                '/' + totalLength);
                            r.addHeader( "Accept-Ranges", "bytes");
                            r.addHeader("Connection", "close");
                            return r;
                        }else {
                            return newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, "text/plain",
                                    "Range request not satisfiable");
                        }
                    }else {
                        //Workaround : NanoHTTPD is using the InputStream.available method incorrectly
                        // see RangeInputStream.available
                        retInputStream = new RangeInputStream(retInputStream, 0, totalLength);
                        Response r = newFixedLengthResponse(Response.Status.OK, getMimeType(uri),
                                retInputStream, totalLength);

                        r.addHeader("ETag", etag);
                        r.addHeader("Content-Length", String.valueOf(totalLength));
                        r.addHeader("Connection", "close");
                        r.addHeader("Cache-Control", "cache, max-age=86400");
                        return r;
                    }


                }else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain",
                            "Not found within zip: " + pathInZip);
                }
            }catch(IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                        "Exception : " + e.toString());
            }

        }

        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Server running");
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
    public void mountZip(String mountPath, String zipPath) {
        mountedEPUBs.put(mountPath, new MountedZip(mountPath, zipPath));
    }

    /**
     * Unmount a zip that was mounted with mountZip
     *
     * @param mountPath The mount path given to mount the zip
     */
    public void unmountZip(String mountPath) {
        mountedEPUBs.remove(mountPath);
    }

    public void addFilter(String mountPath, String extension, String regex, String replacement) {
        MountedZip mountedZip = mountedEPUBs.get(mountPath);
        mountedZip.addFilter(extension, regex, replacement);
    }

    /**
     * Class that represents a mounted zip on HTTP
     */
    public class MountedZip {

        public String mountPath;

        public String zipPath;

        public HashMap<String, List<MountedZipFilter>> filters;

        private ZipFile zipFile;

        public MountedZip(String mountPath, String zipPath) {
            this.mountPath = mountPath;
            this.zipPath = zipPath;
        }

        public ZipFile getZipFile() throws IOException{
            if(zipFile == null) {
                zipFile = new ZipFile(zipPath);
            }

            return zipFile;
        }

        public void addFilter(String extension, String regex, String replacement) {
            if(filters == null) {
                filters = new HashMap<>();
            }

            List<MountedZipFilter> filterList = filters.get(extension);
            if(filterList == null) {
                filterList = new ArrayList<>();
                filters.put(extension, filterList);
            }

            MountedZipFilter filter = new MountedZipFilter(
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE), replacement);
            filterList.add(filter);
        }

        public boolean hasFilter(String extension) {
            return filters != null && filters.containsKey(extension);
        }

        public String filterEntry(String extension, String content) {
            List<MountedZipFilter> entryFilterList = filters.get(extension);
            if(entryFilterList == null) {
                return content;
            }

            MountedZipFilter currentFilter = null;
            for(int i = 0; i < entryFilterList.size(); i++) {
                currentFilter = entryFilterList.get(i);
                content = currentFilter.pattern.matcher(content).replaceAll(currentFilter.replacement);
            }

            return content;
        }

    }

    private class MountedZipFilter {

        private MountedZipFilter(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public Pattern pattern;

        public String replacement;

    }





}
