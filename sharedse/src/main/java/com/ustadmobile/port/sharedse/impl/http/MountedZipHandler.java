package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.util.UMFileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
 * A RouterNanoHTTPD UriResponder that when mounted serves files from the zip for content viewing
 * purposes. It will replace autoplay with data-autoplay so that the autoplay can be triggered
 * by javascript when the WebView is actually in view (rather than when it is loaded).
 *
 * Created by mike on 8/30/16.
 */
public class MountedZipHandler extends FileResponder implements RouterNanoHTTPD.UriResponder {

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

    public static class FilteredSource implements IFileSource {

        private IFileSource src;

        private List<MountedZipFilter> filters;

        private ByteArrayInputStream inputStream;

        private long length = -1;

        public FilteredSource(IFileSource src, List<MountedZipFilter> filters) {
            this.src = src;
            this.filters = filters;
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
                byte[] buf = new byte[1024];
                int bytesRead = -1;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                while((bytesRead = srcIn.read(buf, 0, buf.length)) != -1) {
                    bout.write(buf, 0, bytesRead);
                }
                srcIn.close();
                String content = new String(bout.toByteArray(), "UTF-8");

                MountedZipFilter currentFilter = null;
                for(int i = 0; i < filters.size(); i++) {
                    currentFilter = filters.get(i);
                    content = currentFilter.pattern.matcher(content).replaceAll(currentFilter.replacement);
                }
                byte[] filteredBytes = content.getBytes("UTF-8");
                length = filteredBytes.length;
                inputStream = new ByteArrayInputStream(filteredBytes);
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

        HashMap<String, List<MountedZipHandler.MountedZipFilter>> filters =
                uriResource.initParameter(1, HashMap.class);
        String extension = UMFileUtil.getExtension(pathInZip);
        if(filters != null && filters.containsKey(extension)) {
            src = new FilteredSource(src, filters.get(extension));
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
