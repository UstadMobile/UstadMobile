package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.OpdsEndpoint;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.port.sharedse.networkmanager.EntryStatusTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Nano Httpd Uri Responder to provide OPDS indexes, EPUB files, partial contents of EPUB files.
 *
 * It takes the following initilization parameters:
 * 0: Context object : used to communicate with the catalog controller
 * 1: Empty WeakHashMap : used to cache ZipFile objects for delivering responses to entry files
 *
 * It makes the following available over HTTP:
 *
 * /catalog/acquire.opds - Acquisition feed listing all known entries on this device
 * /catalog/entry/uuid - Provides the entry file (e.g. epub)
 * /catalog/entry/uuid/some/file - Where the entry is a zip (e.g. epub) this directly serves some/file from the zip container
 *
 * Created by mike on 2/21/17.
 */
public class CatalogUriResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {

    public static final String ENTRY_PATH_COMPONENT = "/entry/";

    public static final int INIT_PARAM_INDEX_CONTEXT = 0;

    public static final int INIT_PARAM_INDEX_HASHMAP = 1;

    public static final int INIT_PARAM_INDEX_EMBEDDEDHTTPD = 2;

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        try {
            Object context = getContext(uriResource);
            if(normalizedUri.endsWith("acquire.opds")) {
                UstadJSOPDSFeed deviceFeed = (UstadJSOPDSFeed)OpdsEndpoint.getInstance().loadItem(
                        OpdsEndpoint.OPDS_PROTO_DEVICE, null, context, null);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                deviceFeed.serialize(bout);
                bout.flush();
                ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadJSOPDSItem.TYPE_ACQUISITIONFEED, bin, bout.size());
                return r;
            }else if(normalizedUri.contains(ENTRY_PATH_COMPONENT)) {
                return handleEntryRequest(uriResource, NanoHTTPD.Method.GET, session, normalizedUri);
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
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());

        try {
            if(normalizedUri.endsWith("/entry_status")) {
                return handleEntryStatusRequest(uriResource, NanoHTTPD.Method.GET, session,
                        normalizedUri);
            }
        }catch (NanoHTTPD.ResponseException e) {
            e.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", e.toString());
        }catch(IOException e) {
            e.printStackTrace();
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
                    "text/plain", e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain",
                "No such post endpoint");
    }

    private Object getContext(RouterNanoHTTPD.UriResource uriResource) {
        return uriResource.initParameter(INIT_PARAM_INDEX_CONTEXT, Object.class);
    }

    public NanoHTTPD.Response handleEntryRequest(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.Method method, NanoHTTPD.IHTTPSession session, String normalizedUri) throws IOException{
        normalizedUri = normalizedUri != null ? normalizedUri : RouterNanoHTTPD.normalizeUri(session.getUri());
        int[] containerIdRange = getEntryUuidSubstringRange(normalizedUri);
        String uuid = normalizedUri.substring(containerIdRange[0], containerIdRange[1]);
        EmbeddedHTTPD httpd = uriResource.initParameter(INIT_PARAM_INDEX_EMBEDDEDHTTPD,
                EmbeddedHTTPD.class);


        CatalogEntryInfo info = CatalogPresenter.getEntryInfo(uuid, CatalogPresenter.SHARED_RESOURCE,
                getContext(uriResource));
        if(info == null) {
            //this container does not exist here anymore
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                    "text/plain", "Container " + uuid + " not found by catalog controller");
        }

        File containerFile = new File(info.fileURI);
        if(containerIdRange[1] == normalizedUri.length()) {
            //this is the end of the path : serve the container file itself
            NanoHTTPD.Response entryResponse = newResponseFromFile(method, uriResource,
                    session, new FileSource(containerFile));

            if(entryResponse.getData() != null) { //null data = HEAD method response with no data
                ResponseMonitoredInputStream streamMonitor = new ResponseMonitoredInputStream(
                        entryResponse.getData(), entryResponse);
                streamMonitor.setOnCloseListener(httpd);
                entryResponse.setData(streamMonitor);
                httpd.handleResponseStarted(entryResponse);
                return entryResponse;
            }

            return entryResponse;
        }else {
            //serve a particular file from the container
            String pathInZip = normalizedUri.substring(containerIdRange[1] + 1);
            WeakHashMap zipMap = uriResource.initParameter(INIT_PARAM_INDEX_HASHMAP, WeakHashMap.class);
            ZipFile zipFile;
            if(zipMap.containsKey(info.fileURI)) {
                zipFile = (ZipFile)zipMap.get(info.fileURI);
            }else {
                zipFile = new ZipFile(containerFile);
                zipMap.put(info.fileURI, zipFile);
            }

            ZipEntry fileEntry = zipFile.getEntry(pathInZip);
            return newResponseFromFile(method, uriResource, session, new ZipEntrySource(fileEntry, zipFile));
        }
    }

    public NanoHTTPD.Response handleEntryStatusRequest(RouterNanoHTTPD.UriResource uriResource,
                                                       NanoHTTPD.Method method,
                                                       NanoHTTPD.IHTTPSession session,
                                                       String normalizedUri) throws IOException, NanoHTTPD.ResponseException {

        HashMap<String, String> files = new HashMap<>();
        session.parseBody(files);
        String jsonRequest = session.getQueryParameterString();
        JSONObject requestJsonObj = new JSONObject(jsonRequest);
        JSONArray requestEntryIds = requestJsonObj.getJSONArray(EntryStatusTask.ENTRY_RESPONSE_ENTRIES_KEY);

        JSONObject responseJsonObj = new JSONObject();
        JSONObject responseEntries = new JSONObject();
        responseJsonObj.put(EntryStatusTask.ENTRY_RESPONSE_ENTRIES_KEY, responseEntries);

        Object context = getContext(uriResource);

        boolean available;
        CatalogEntryInfo entryInfo;
        String entryId;
        JSONObject entryObj;
        for(int i = 0; i < requestEntryIds.length(); i++) {
            entryId = requestEntryIds.getString(i);
            entryInfo = CatalogPresenter.getEntryInfo(entryId,
                    CatalogPresenter.SHARED_RESOURCE, context);
            entryObj = new JSONObject();
            responseEntries.put(entryId, entryObj);
            available = entryInfo != null
                    && entryInfo.acquisitionStatus == CatalogPresenter.STATUS_ACQUIRED;
            entryObj.put(EntryStatusTask.ENTRY_RESPONSE_KEY_AVAILABLE, available);
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                "application/json", responseJsonObj.toString());
    }

    public NanoHTTPD.Response handleEntryRequest(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.Method method, NanoHTTPD.IHTTPSession session) throws IOException {
        return handleEntryRequest(uriResource, method, session, RouterNanoHTTPD.normalizeUri(session.getUri()));
    }


    /**
     * For a /catalog/entry request where the UI is in the form of /catalog/entry/course-uuid
     * process it and return the first and last character indexes (for use with substring)
     *
     * @param uri catalog entry uri as above
     * @return The uuid for this request
     */
    private int[] getEntryUuidSubstringRange(String uri) {
        int containerIdStart = uri.indexOf(ENTRY_PATH_COMPONENT)
                + ENTRY_PATH_COMPONENT.length();
        int containerIdEnd = uri.indexOf('/', containerIdStart);
        if(containerIdEnd == -1)
            containerIdEnd = uri.length();

        return new int[]{containerIdStart, containerIdEnd};
    }


    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }


    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        try {
            if(NanoHTTPD.Method.HEAD.toString().equalsIgnoreCase(method)) {
                String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());
                if(normalizedUri.contains(ENTRY_PATH_COMPONENT)) {
                    return handleEntryRequest(uriResource, NanoHTTPD.Method.HEAD, session);
                }
            }
        }catch(IOException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    "Exception:"  + e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                "Request not understood by .other method: " + method);
    }
}
