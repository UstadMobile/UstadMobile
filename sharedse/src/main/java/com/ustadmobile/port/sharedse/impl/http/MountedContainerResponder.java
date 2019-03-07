package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.port.sharedse.container.ContainerManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class MountedContainerResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {

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

    public static class FilteredHtmlSource implements FileResponder.IFileSource {

        private FileResponder.IFileSource src;

        private ByteArrayInputStream inputStream;

        private long length = -1;

        private String scriptPath;

        public FilteredHtmlSource(FileResponder.IFileSource src, String scriptPath) {
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
            return src.exists();
        }

        @Override
        public String getName() {
            return src.getName();
        }
    }


    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String requestUri = RouterNanoHTTPD.normalizeUri(session.getUri());
        String pathInContainer = requestUri.substring(
                uriResource.getUri().length() - URI_ROUTE_POSTFIX.length());
        ContainerManager container = uriResource.initParameter(0, ContainerManager.class);
        ContainerEntryWithContainerEntryFile entry = container.getEntry(pathInContainer);
        if(entry == null){
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                    "text/plain", "Entry not found in container");
        }

        return newResponseFromFile(uriResource, session,
                new FileSource(new File(entry.getContainerEntryFile().getCefPath())));
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
