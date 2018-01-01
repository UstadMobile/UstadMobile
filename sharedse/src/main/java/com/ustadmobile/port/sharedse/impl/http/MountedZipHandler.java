package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.URLTextUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * A RouterNanoHTTPD UriResponder that when mounted serves files from the zip for content viewing
 * purposes. It will replace autoplay with data-autoplay so that the autoplay can be triggered
 * by javascript when the WebView is actually in view (rather than when it is loaded).
 *
 * Initialization parameters:
 *  ZipFile object representing the zip to be mounted
 *  Boolean epubHtmlFilterEnabled - true to enable epub filter for pagination, autoplay control - false otherwise
 *  String epubScriptPath - the script src to add if
 *
 * Created by mike on 8/30/16.
 */
public class MountedZipHandler extends FileResponder implements RouterNanoHTTPD.UriResponder {

    /**
     * The string that is added
     */
    public static final String URI_ROUTE_POSTFIX = "(.)+";

    private static final ArrayList<String> HTML_EXTENSIONS = new ArrayList<>();

    static {
        HTML_EXTENSIONS.add("xhtml");
        HTML_EXTENSIONS.add("html");
        HTML_EXTENSIONS.add("htm");
    }

    public static class MountedZipFilter {

        public MountedZipFilter(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public Pattern pattern;

        public String replacement;
    }

    public static class FilteredHtmlSource implements IFileSource {

        private IFileSource src;

        private ByteArrayInputStream inputStream;

        private long length = -1;

        private String scriptPath;

        public FilteredHtmlSource(IFileSource src, String scriptPath) {
            this.src = src;
            this.scriptPath = scriptPath;
        }

        @Override
        public long getLength() {
            try { getInputStream(); }
            catch(IOException e) {}
            return length;
        }

        @Override
        public long getLastModifiedTime() {
            return src.getLastModifiedTime();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if(inputStream == null) {
                //init and filter
                InputStream srcIn = src.getInputStream();
                try {
                    EpubHtmlFilterSerializer filterSerializer = new EpubHtmlFilterSerializer();
                    filterSerializer.setScriptSrcToAdd(scriptPath);
                    filterSerializer.setIntput(srcIn);
                    byte[] filteredBytes = filterSerializer.getOutput();
                    length = filteredBytes.length;
                    inputStream = new ByteArrayInputStream(filteredBytes);
                }catch(XmlPullParserException x) {
                    throw new IOException(x);
                }
            }
            return inputStream;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public String getName() {
            return src.getName();
        }
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String requestUri = RouterNanoHTTPD.normalizeUri(session.getUri());
        String pathInZip = requestUri.substring(
                uriResource.getUri().length() - URI_ROUTE_POSTFIX.length());
        ZipFile zipFile = uriResource.initParameter(0, ZipFile.class);
        ZipEntry entry = zipFile.getEntry(pathInZip);

        if(session.getUri().endsWith("/")) {
            return listDirectory(pathInZip, zipFile);
        }

        IFileSource src = new ZipEntrySource(entry, zipFile);
        String extension = UMFileUtil.getExtension(pathInZip);

        if(uriResource.initParameter(1, Boolean.class) && HTML_EXTENSIONS.contains(extension)) {
            src = new FilteredHtmlSource(src, uriResource.initParameter(2, String.class));
        }

        return newResponseFromFile(uriResource, session, src);
    }

    public NanoHTTPD.Response listDirectory(String dirInZip, ZipFile zipfile) {
        StringBuffer xhtmlBuffer = new StringBuffer();
        xhtmlBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\">")
                .append(" <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" ")
                .append(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> \n")
                .append("  <html xmlns=\"http://www.w3.org/1999/xhtml\"> \n")
                .append("<body>");

        if(!dirInZip.endsWith("/"))
            dirInZip += "/";

        Enumeration<? extends ZipEntry> entries = zipfile.entries();
        List<String> filesInDir = new ArrayList<>();
        List<String> subdirs = new ArrayList<>();

        ZipEntry currentEntry;
        String currentDirName;

        String pathAfterDir;
        int lastSepPos;
        while(entries.hasMoreElements()) {
            currentEntry = entries.nextElement();
            if(currentEntry.getName().substring(0, dirInZip.length()).equals(dirInZip)) {
                pathAfterDir = currentEntry.getName().substring(dirInZip.length());

                lastSepPos = pathAfterDir.indexOf('/');
                if(lastSepPos == -1) {
                    //no further paths, this is a file
                    filesInDir.add(pathAfterDir);
                }else {
                    pathAfterDir = pathAfterDir.substring(0, lastSepPos);
                    if(!subdirs.contains(pathAfterDir))
                        subdirs.add(pathAfterDir);
                }

            }
        }

        xhtmlBuffer.append("<h2>Subdirectories</h2>\n<ul>");
        appendEntryLinksToBuffer(subdirs, xhtmlBuffer);
        xhtmlBuffer.append("</ul><h2>Files</h2><ul>");
        appendEntryLinksToBuffer(filesInDir, xhtmlBuffer);
        xhtmlBuffer.append("</ul></body></html>");

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                "application/xhtml+xml", xhtmlBuffer.toString());
    }

    private void appendEntryLinksToBuffer(List<String> entries, StringBuffer buffer) {
        for(String entry : entries) {
            buffer.append("<li><a href=\"").append(URLTextUtil.urlEncodeUTF8(entry))
                    .append("</a></li>\n");
        }
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
