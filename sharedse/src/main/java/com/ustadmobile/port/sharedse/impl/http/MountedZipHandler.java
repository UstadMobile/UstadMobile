package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.util.UMFileUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        String pathInZip = RouterNanoHTTPD.normalizeUri(session.getUri()).substring(
                uriResource.getUri().length() - URI_ROUTE_POSTFIX.length());
        ZipFile zipFile = uriResource.initParameter(0, ZipFile.class);
        ZipEntry entry = zipFile.getEntry(pathInZip);
        IFileSource src = new ZipEntrySource(entry, zipFile);
        String extension = UMFileUtil.getExtension(pathInZip);

        if(uriResource.initParameter(1, Boolean.class) && HTML_EXTENSIONS.contains(extension)) {
            src = new FilteredHtmlSource(src, uriResource.initParameter(2, String.class));
        }

        return newResponseFromFile(uriResource, session, src);
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
