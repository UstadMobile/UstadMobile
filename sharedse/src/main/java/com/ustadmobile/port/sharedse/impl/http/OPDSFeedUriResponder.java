package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Uri responder to provide a feed of courses that have been shared by the user.
 *
 * Created by mike on 6/15/17.
 */
public class OPDSFeedUriResponder implements RouterNanoHTTPD.UriResponder {

    public NanoHTTPD.Response serve(NanoHTTPD.Method method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        UstadJSOPDSFeed returnFeed = uriResource.initParameter(0, UstadJSOPDSFeed.class);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        NanoHTTPD.Response response = null;
        try {
            returnFeed.serialize(bout);
            byte[] byteArr = bout.toByteArray();
            ByteArrayInputStream bin = new ByteArrayInputStream(byteArr);
            response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadJSOPDSItem.TYPE_ACQUISITIONFEED, bin, byteArr.length);
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 81, null, e);
        }

        return response;
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return serve(NanoHTTPD.Method.GET, uriResource, urlParams, session);
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return serve(NanoHTTPD.Method.POST, uriResource, urlParams, session);
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
