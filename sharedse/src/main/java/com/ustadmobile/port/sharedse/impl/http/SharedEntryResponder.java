package com.ustadmobile.port.sharedse.impl.http;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.OpdsEntry;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class SharedEntryResponder implements RouterNanoHTTPD.UriResponder{

    public static final int ARG_INDEX_FEED_UUID = 0;

    public static final int ARG_INDEX_CONTEXT = 1;

    public static final int ARG_INDEX_PORTNUM = 2;

    public static final String MIRROR_PATH = "/mirror/";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String opdsUuid = uriResource.initParameter(ARG_INDEX_FEED_UUID, String.class);
        Object context = uriResource.initParameter(ARG_INDEX_CONTEXT, Object.class);
        Integer portNum = uriResource.initParameter(ARG_INDEX_PORTNUM, Integer.class);

        OpdsEntry entry = UmAppDatabase.getInstance(context).getOpdsEntryWithRelationsDao()
                .getEntryByUuidStatic(opdsUuid);

        SharedEntryInfo sharedEntryInfo = new SharedEntryInfo(opdsUuid, entry.getUrl(),
            portNum, MIRROR_PATH);
        String jsonStr = new Gson().toJson(sharedEntryInfo);

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                jsonStr);
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
