package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.util.UMFileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 8/30/16.
 */
public class MountedZipHandler implements RouterNanoHTTPD.UriResponder {

    /**
     * The string that is added
     */
    public static final String URI_ROUTE_POSTFIX = "(.)+";

    public static class MountedZipFilter {

        public MountedZipFilter(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public Pattern pattern;

        public String replacement;
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        NanoHTTPD.Response response = null;
        int range[];
        String ifNoneMatchHeader;
        InputStream retInputStream;

        String pathInZip = RouterNanoHTTPD.normalizeUri(session.getUri()).substring(
                uriResource.getUri().length() - URI_ROUTE_POSTFIX.length());
        try {
            ZipFile zipFile = uriResource.initParameter(0, ZipFile.class);
            HashMap<String, List<MountedZipHandler.MountedZipFilter>> filters = uriResource.initParameter(1, HashMap.class);
            ZipEntry entry = zipFile.getEntry(pathInZip);

            if(entry != null) {
                int totalLength = (int)entry.getSize();
                String etag = Integer.toHexString((pathInZip + entry.getTime() + "" +
                        totalLength).hashCode());
                String extension = UMFileUtil.getExtension(pathInZip);
                InputStream zipEntryStream = zipFile.getInputStream(entry);
                retInputStream = zipEntryStream;

                ifNoneMatchHeader = session.getHeaders().get("if-none-match");
                if(ifNoneMatchHeader != null && ifNoneMatchHeader.equals(etag)) {
                    NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED,
                            EmbeddedHTTPD.getMimeType(pathInZip), null);
                    r.addHeader("ETag", etag);
                    r.addHeader("Connection", "close");
                    return r;
                }


                range = parseRangeRequest(session, totalLength);

                //see if this is an entry we need to filter.
                if(extension != null) {
                    if(filters != null && filters.containsKey(extension)) {
                        byte[] buf = new byte[1024];
                        int bytesRead = -1;
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        while((bytesRead = zipEntryStream.read(buf, 0, buf.length)) != -1) {
                            bout.write(buf, 0, bytesRead);
                        }
                        zipEntryStream.close();
                        String contentStr = new String(bout.toByteArray(), "UTF-8");
                        contentStr = filterEntry(filters.get(extension), contentStr);
                        byte[] strBytes = contentStr.getBytes("UTF-8");
                        retInputStream = new ByteArrayInputStream(strBytes);
                        totalLength = strBytes.length;
                    }
                }

                if(range != null) {
                    if(range[0] != -1) {
                        retInputStream = new RangeInputStream(retInputStream, range[0], range[1]);
                        int contentLength = (range[1]+1) - range[0];
                        NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                                EmbeddedHTTPD.getMimeType(pathInZip), retInputStream, contentLength);

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
                        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain",
                                "Range request not satisfiable");
                    }
                }else {
                    //Workaround : NanoHTTPD is using the InputStream.available method incorrectly
                    // see RangeInputStream.available
                    retInputStream = new RangeInputStream(retInputStream, 0, totalLength);
                    NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                            EmbeddedHTTPD.getMimeType(pathInZip), retInputStream, totalLength);

                    r.addHeader("ETag", etag);
                    r.addHeader("Content-Length", String.valueOf(totalLength));
                    r.addHeader("Connection", "close");
                    r.addHeader("Cache-Control", "cache, max-age=86400");
                    return r;
                }


            }else {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain",
                        "Not found within zip: " + pathInZip);
            }
        }catch(IOException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    "Exception : " + e.toString());
        }
    }

    private int[] parseRangeRequest(NanoHTTPD.IHTTPSession session, int totalLength) {
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

    public String filterEntry(List<MountedZipFilter> entryFilterList, String content) {
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


    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }


}
