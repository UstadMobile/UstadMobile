package com.ustadmobile.port.android.impl.http;

import android.util.Log;

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
import java.util.zip.ZipInputStream;

import fi.iki.elonen.*;

/**
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
                    range[1] = totalLength;
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
                return new  Response(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: only zip is mentioned, not path within:" + uri);
            }
            String zipMountPath = uri.substring(PREFIX_MOUNT.length(), nextSlash);

            if(!mountedEPUBs.containsKey(zipMountPath)) {
                return new Response(Response.Status.NOT_FOUND, "text/html",
                        "Invalid: that zip is not mounted: " + uri);
            }

            String pathInZip = uri.substring(nextSlash + 1);

            try {
                MountedZip mountedZip = mountedEPUBs.get(zipMountPath);
                ZipFile zipFile = new ZipFile(mountedZip.zipPath);
                ZipEntry entry = zipFile.getEntry(pathInZip);
                int totalLength = (int)entry.getSize();
                String etag = Integer.toHexString((zipMountPath + pathInZip + entry.getTime() + "" +
                        totalLength).hashCode());
                if(entry != null) {
                    String extension = UMFileUtil.getExtension(pathInZip);
                    InputStream zipEntryStream = zipFile.getInputStream(entry);
                    retInputStream = zipEntryStream;

                    ifNoneMatchHeader = session.getHeaders().get("If-None-Match");
                    if(ifNoneMatchHeader != null && ifNoneMatchHeader.equals(etag)) {
                        Response r = new Response(Response.Status.NOT_MODIFIED, getMimeType(uri), "");
                        r.addHeader("ETag", etag);
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
                            retInputStream = new ByteArrayInputStream(contentStr.getBytes("UTF-8"));
                        }
                    }

                    if(range != null) {
                        if(range[0] != -1) {
                            retInputStream = new RangeInputStream(retInputStream, range[0], range[1]);
                            Response r = new Response(Response.Status.PARTIAL_CONTENT, getMimeType(uri),
                                    retInputStream);
                            r.addHeader("ETag", etag);
                            r.addHeader("Content-Length", String.valueOf(range[1] - range[0]));
                            r.addHeader("Content-Range", "bytes " + range[0] + '-' + range[1] +
                                '/' + totalLength);
                            r.addHeader( "Accept-Ranges", "bytes");
                            return r;
                        }else {
                            return new Response(Response.Status.RANGE_NOT_SATISFIABLE, "text/plain",
                                    "Range request not satisfiable");
                        }

                    }else {
                        Response r = new Response(Response.Status.OK, getMimeType(uri),
                                retInputStream);
                        r.addHeader("ETag", etag);
                        r.addHeader("Content-Length", String.valueOf(totalLength));
                        return r;
                    }


                }else {
                    return new Response(Response.Status.NOT_FOUND, "text/plain", "Not found within zip: "
                        + pathInZip);
                }
            }catch(IOException e) {
                return new Response(Response.Status.INTERNAL_ERROR, "text/plain", "Exception: "
                    + e.toString());
            }

        }


        String msg = "<html><body><h1>Hello server</h1>\n";
        Map<String, String> parms = session.getParms();
        if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else {
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        }
        return new Response(msg);
    }

    public void mountZip(String mountPath, String zipPath) {
        mountedEPUBs.put(mountPath, new MountedZip(mountPath, zipPath));
    }

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

        public MountedZip(String mountPath, String zipPath) {
            this.mountPath = mountPath;
            this.zipPath = zipPath;
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
