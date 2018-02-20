package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * The OpdsFeedUriResponder will take a given opds uuid and serves an OPDS feed where the parent
 * entry is used for the feed information itself, and child OpdsEntry objects as per the
 * OpdsParentToChildJoin table are listed as entry tags within the feed as per the OPDS spec
 *
 * This UriResponder takes three arguments (to be provided when calling addRoute):
 *
 *  - the uuid of the parent entry. All direct child entries will be listed
 *  - EmbeddedHTTPD.ResponseListener responseListener to call when finished serving
 *  - Context object (for database access)
 *
 * Created by mike on 6/15/17.
 */
public class OPDSFeedUriResponder implements RouterNanoHTTPD.UriResponder {

    private static final int ARG_INDEX_FEED_UUID = 0;

    private static final int ARG_INDEX_RESPONSE_LISTENER = 1;

    private static final int ARG_INDEX_CONTEXT = 2;

    public NanoHTTPD.Response serve(NanoHTTPD.Method method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String feedUuid = uriResource.initParameter(ARG_INDEX_FEED_UUID, String.class);
        final EmbeddedHTTPD.ResponseListener responseListener = uriResource.initParameter(
                ARG_INDEX_RESPONSE_LISTENER, EmbeddedHTTPD.ResponseListener.class);
        final Object context = uriResource.initParameter(ARG_INDEX_CONTEXT, Object.class);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        NanoHTTPD.Response response = null;
        try {
            DbManager dbManager = DbManager.getInstance(context);
            OpdsEntryWithRelations feedParent = dbManager.getOpdsEntryWithRelationsDao()
                    .getEntryByUuidStatic(feedUuid);
            List<OpdsEntryWithRelations> childEntries = dbManager.getOpdsEntryWithRelationsDao()
                    .getEntriesByParentAsListStatic(feedUuid);

            XmlSerializer serializer = UstadMobileSystemImpl.getInstance().newXMLSerializer();
            serializer.setOutput(bout, "UTF-8");
            feedParent.serializeFeed(serializer, childEntries);
            byte[] byteArr = bout.toByteArray();
            ByteArrayInputStream bin = new ByteArrayInputStream(byteArr);
            ResponseMonitoredInputStream rin = new ResponseMonitoredInputStream(bin);
            response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadJSOPDSItem.TYPE_ACQUISITIONFEED, rin, byteArr.length);
            rin.setResponse(response);

            if(responseListener != null) {
                rin.setOnCloseListener(responseListener::responseFinished);
            }
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 81, null, e);
        }

        if(responseListener != null) {
            responseListener.responseStarted(response);
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
