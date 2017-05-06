package com.ustadmobile.test.core.impl.se;

import com.ustadmobile.port.sharedse.impl.http.FileResponder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Implemented for purposes of running unit tests. This HTTP server will serve resources from
 * the classpath using getClass.getResource
 */

public class ClassResourcesResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {


    public static Hashtable LAST_MODIFIED_TIMES = new Hashtable();

    public static class ResourceFileSource implements FileResponder.IFileSource {

        private int length;

        private long lastModifiedTime;

        private byte[] contentBuf;

        private String name;

        public ResourceFileSource(URL resourcePath, long lastModifiedTime) {
            InputStream resIn = null;

            if(resourcePath != null) {
                name = resourcePath.getFile();
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                try {
                    resIn = resourcePath.openStream();
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    while((bytesRead = resIn.read(buf)) != -1) {
                        bout.write(buf, 0, bytesRead);
                    }
                }catch(IOException e) {

                }finally {
                    if(resIn != null) {
                        try { resIn.close(); }
                        catch(IOException e) {}
                    }
                }

                this.contentBuf = bout.toByteArray();
                this.lastModifiedTime = lastModifiedTime;
            }
        }

        @Override
        public long getLength() {
            return contentBuf != null ? contentBuf.length : -1;
        }

        @Override
        public long getLastModifiedTime() {
            return lastModifiedTime;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contentBuf);
        }

        @Override
        public boolean exists() {
            return contentBuf != null;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String prefix = uriResource.initParameter(0, String.class);
        String resPath = '/' + session.getUri().substring(prefix.length());
        ResourceFileSource fileSource = new ResourceFileSource(getClass().getResource(resPath), new Date().getTime());
        return newResponseFromFile(NanoHTTPD.Method.GET, uriResource, session, fileSource);
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
