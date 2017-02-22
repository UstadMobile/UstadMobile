package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.controller.CatalogController;
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
 * Nano Httpd Uri Responder to provide OPDS indexes, EPUB files, partial contents of EPUB files
 *
 * Created by mike on 2/21/17.
 */
public class CatalogUriResponder implements RouterNanoHTTPD.UriResponder {

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Object context = uriResource.initParameter(0, Object.class);
        try {
            if(normalizedUri.endsWith("acquire.opds")) {
                UstadJSOPDSFeed deviceFeed = CatalogController.makeDeviceFeed(
                        impl.getStorageDirs(CatalogController.SHARED_RESOURCE, context),
                        CatalogController.SHARED_RESOURCE, "/catalog/", CatalogController.LINK_HREF_MODE_ID,
                        context);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                deviceFeed.serialize(bout);
                bout.flush();
                ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadJSOPDSItem.TYPE_ACQUISITIONFEED, bin, bout.size());
                return r;
            }
        }catch(IOException e) {
            e.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain",
                "No such catalog available");
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
