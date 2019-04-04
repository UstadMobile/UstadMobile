package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContainerEntryFile;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class ContainerEntryFileResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {

    public static final int INIT_PARAM_DB_INDEX = 0;

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource,
                                  Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UmAppDatabase db = uriResource.initParameter(INIT_PARAM_DB_INDEX, UmAppDatabase.class);

        String url = RouterNanoHTTPD.normalizeUri(session.getUri());
        int lastSlashPos = url.lastIndexOf('/');
        if(lastSlashPos == -1) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", null);
        }

        String entryFileUid = url.substring(lastSlashPos + 1);
        try {
            ContainerEntryFile entryFile = db.getContainerEntryFileDao().findByUid(
                    Long.parseLong(entryFileUid));
            if(entryFile == null || entryFile.getCefPath() == null) {
                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                        "application/octet", null);
            }

            File file = new File(entryFile.getCefPath());
            return newResponseFromFile(uriResource, session, new FileSource(file));
        }catch(NumberFormatException ne){
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "application/octet", null);
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
