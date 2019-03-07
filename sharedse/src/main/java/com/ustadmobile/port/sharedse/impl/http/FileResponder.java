package com.ustadmobile.port.sharedse.impl.http;
import com.ustadmobile.core.util.UMFileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * This is a RouterNanoHTTPD Responder that can be used to serve files from the file system or
 * files from within a zip. It can handle etags, validating if-not-modified requests, and partial
 * requests. Will return a 404 response if the file does not exist. This has been tested to
 * successfully serve streaming video and audio over http to a WebView.
 *
 * To serve from a file:
 *
 * return FileResponder.newResponseFromFile(uriResource, session, new FileResponder.FileSource(file))
 *
 * To serve an entry from a zip file:
 *
 * return FileResponder.newResponseFromFile(uriResource, session, new FileResponder.ZipEntrySource(zipEntry, zipFile))
 *
 * Created by mike on 2/22/17.
 */
public abstract class FileResponder {


    /**
     * Interface used to describe a file or file like source to serve an HTTP request: in our case
     * this can be a File or a ZipEntry
     */
    public interface IFileSource {

        /**
         * The total length of the response: use for content-length header
         * @return the total length of the response in bytes for the content-length header
         */
        long getLength();

        /**
         * The last modified time in ms since the epoch
         *
         * @return The last modified time in ms since the epoch
         */
        long getLastModifiedTime();

        /**
         * Get the input stream for the data
         *
         * @return InputStream for the data
         * @throws IOException
         */
        InputStream getInputStream() throws IOException;

        /**
         * Determine if the file or zip entry exists
         *
         * @return True if file exists, false otherwise
         */
        boolean exists();

        /**
         * Provides the base name of the file: only currently used for etag generation purposes
         *
         * @return The base name of the file
         */
        String getName();

    }

    public static class FileSource implements IFileSource{
        private File src;

        public FileSource(File src) {
            this.src = src;
        }

        @Override
        public long getLength() {
            return src.length();
        }

        @Override
        public long getLastModifiedTime() {
            return src.lastModified();
        }

        @Override
        public InputStream getInputStream() throws IOException{
            return new BufferedInputStream(new FileInputStream(src));
        }

        @Override
        public boolean exists() {
            return src.exists();
        }

        @Override
        public String getName() {
            return src.getName();
        }
    }

    public static class ZipEntrySource implements IFileSource {

        private FileHeader entry;

        private ZipFile zipFile;

        /**
         *
         * @param entry
         * @param zipFile
         */
        public ZipEntrySource(FileHeader entry, ZipFile zipFile) {
            this.entry = entry;
            this.zipFile = zipFile;
        }

        public ZipEntrySource(ZipFile zipFile, String pathInZip) {
            this.zipFile = zipFile;
            try {
                this.entry = zipFile.getFileHeader(pathInZip);
            }catch(ZipException e) {

            }
        }



        @Override
        public long getLength() {
            return entry.getUncompressedSize();
        }

        @Override
        public long getLastModifiedTime() {
            return entry.getLastModFileTime();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return zipFile.getInputStream(entry);
            }catch(ZipException ze) {
                throw new IOException(ze);
            }
        }

        @Override
        public boolean exists() {
            return entry != null;//must exist if there is an entry here
        }

        @Override
        public String getName() {
            return entry.getFileName();
        }
    }


    /**
     * Create a NanoHTTPD response from a file or file like object (e.g. zip entry). This will handle
     * validating etags (and return 302 not-modified if a if-not-modified header was sent). It will
     * also take care of responding to partial range requests. It will respond 404 if the file does
     * not exist.
     *
     * @param method The HTTP method being used : We support GET (default) and HEAD (for headers only with no response body)
     * @param uriResource uriResource from the request
     * @param session session from the request
     * @param file Interface representing the file or file like source
     * @param cacheControlHeader The cache control header to put on the response. Optional: can be null for no cache-control header
     * @return An appropriate NanoHTTPD.Response as above for the request
     */
    public static NanoHTTPD.Response newResponseFromFile(NanoHTTPD.Method method, RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.IHTTPSession session, IFileSource file, String cacheControlHeader) {
        boolean isHeadRequest = method.equals(NanoHTTPD.Method.HEAD);
        try {
            long range[];
            String ifNoneMatchHeader;
            InputStream retInputStream;

            if(!file.exists()) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain",
                        isHeadRequest ? null : "File not found");
            }

            long totalLength = file.getLength();
            long lastModifiedTime = file.getLastModifiedTime();
            String fileName = file.getName();
            String etagNameInput = fileName;
            String mimeType = EmbeddedHTTPD.getMimeType(session.getUri());



            //Check to see if the etag provided by the client matches: in which case we can send 302 not modified
            String etag = Integer.toHexString((file.getName() + lastModifiedTime + "" +
                    totalLength).hashCode());
            String extension = UMFileUtil.getExtension(fileName);
            ifNoneMatchHeader = session.getHeaders().get("if-none-match");
            if(ifNoneMatchHeader != null && ifNoneMatchHeader.equals(etag)) {
                NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED,
                        EmbeddedHTTPD.getMimeType(fileName), null);
                r.addHeader("ETag", etag);
                return r;
            }

            range = parseRangeRequest(session, totalLength);
            retInputStream = isHeadRequest ? null : file.getInputStream();
            if(range != null) {
                if(range[0] != -1) {
                    retInputStream = isHeadRequest ? null : new RangeInputStream(retInputStream, range[0], range[1]);
                    long contentLength = (range[1]+1) - range[0];
                    NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                            EmbeddedHTTPD.getMimeType(fileName), retInputStream, contentLength);

                    r.addHeader("ETag", etag);
                    r.addHeader("Content-Type", mimeType);

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
                            isHeadRequest ? null :  "Range request not satisfiable");
                }
            }else {
                //Workaround : NanoHTTPD is using the InputStream.available method incorrectly
                // see RangeInputStream.available
                retInputStream = isHeadRequest ? null : retInputStream;
                NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    EmbeddedHTTPD.getMimeType(fileName), retInputStream, totalLength);

                r.addHeader("ETag", etag);
                r.addHeader("Content-Length", String.valueOf(totalLength));
                r.addHeader("Connection", "close");
                r.addHeader("Content-Type", mimeType);
                if(cacheControlHeader != null)
                    r.addHeader("Cache-Control", cacheControlHeader);
                return r;
            }
        }catch(IOException e) {
            e.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", isHeadRequest ? null : "Internal exception: " + e.toString());
        }
    }

    public static NanoHTTPD.Response newResponseFromFile(NanoHTTPD.Method method, RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.IHTTPSession session, IFileSource file) {
        return newResponseFromFile(method, uriResource, session, file, "cache, max-age=86400");
    }

    public static NanoHTTPD.Response newResponseFromFile(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.IHTTPSession session, IFileSource file) {
        return newResponseFromFile(NanoHTTPD.Method.GET, uriResource, session, file);
    }

    private static long[] parseRangeRequest(NanoHTTPD.IHTTPSession session, long totalLength) {
        return RangeInputStream.parseRangeRequest(session.getHeaders().get("range"), totalLength);
    }


}
