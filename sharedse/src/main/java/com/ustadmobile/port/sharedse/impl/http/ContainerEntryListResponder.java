package com.ustadmobile.port.sharedse.impl.http;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * NanoHTTPD router that will provide a list of all ContainEntry objects (and their MD5 sum) so that
 * the DownloadJobItemRunner can decide which of those it needs to download, and which of those it
 * already has.
 */
public class ContainerEntryListResponder implements RouterNanoHTTPD.UriResponder  {

    public static final int PARAM_APPDB_INDEX = 0;

    public static final String PARAM_CONTAINER_UID = "containerUid";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UmAppDatabase appDatabase = uriResource.initParameter(PARAM_APPDB_INDEX, UmAppDatabase.class);

        try {
            Long containerUid = session.getParameters().containsKey(PARAM_CONTAINER_UID) &&
                    !session.getParameters().get(PARAM_CONTAINER_UID).isEmpty() ?
                    Long.parseLong(session.getParameters().get(PARAM_CONTAINER_UID).get(0)) : null;
            if(containerUid != null) {
                List<ContainerEntryWithMd5> entryList = appDatabase.getContainerEntryDao()
                        .findByContainerWithMd5(containerUid);
                NanoHTTPD.Response.Status status = entryList.isEmpty() ?
                        NanoHTTPD.Response.Status.NOT_FOUND : NanoHTTPD.Response.Status.OK;
                return NanoHTTPD.newFixedLengthResponse(status, "application/json",
                        new Gson().toJson(entryList));
            }
        }catch(NumberFormatException e) {
            UstadMobileSystemImpl.l(UMLog.WARN, 700,
                    "ContainerEntryListResponder received bad uid");
        }


        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                "application/json", null);
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
