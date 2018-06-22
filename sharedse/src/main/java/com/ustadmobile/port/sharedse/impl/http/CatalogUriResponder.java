package com.ustadmobile.port.sharedse.impl.http;

import com.google.gson.Gson;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;
import com.ustadmobile.lib.db.entities.ContainerFileEntryWithContainerFile;
import com.ustadmobile.port.sharedse.networkmanager.EntryStatusTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

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
 *
 *
 * /catalog/container-dl/entryId[/last-updated] - Provides the entry file (e.g. epub)
 *   entryId - the entryId DOUBLE ENCODED - as NanoHTTPD does not provide access to the raw uri.
 *
 * /catalog/container-content/entryId/last-updated/path-in-container
 *
 * /catalog/entry/uuid/some/file - Where the entry is a zip (e.g. epub) this directly serves some/file from the zip container
 *
 * Created by mike on 2/21/17.
 */
public class CatalogUriResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {

    public static final String ENTRY_PATH_COMPONENT = "/container/";

    public static final String CONTAINER_DL_PATH_COMPONENT = "/container-dl/";

    public static final int INIT_PARAM_INDEX_CONTEXT = 0;

    public static final int INIT_PARAM_INDEX_HASHMAP = 1;

    public static final int INIT_PARAM_INDEX_EMBEDDEDHTTPD = 2;

    public static final String QUERY_ARG_ENTRYID = "entryId";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());

        try {
            if(session.getUri().contains(CONTAINER_DL_PATH_COMPONENT)) {
                return handleContainerFileRequest(uriResource, NanoHTTPD.Method.GET, session, normalizedUri);
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

    public NanoHTTPD.Response handleContainerFileRequest(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.Method method, NanoHTTPD.IHTTPSession session, String normalizedUri) throws IOException{
        String uri = session.getUri();
        String entryId = uri.substring(uri.indexOf(CONTAINER_DL_PATH_COMPONENT) +
            CONTAINER_DL_PATH_COMPONENT.length());

        entryId = URLDecoder.decode(entryId, "UTF-8");

        if(entryId.length() == 0) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST,
                    "text/plain", "no entryid specified");
        }

        EmbeddedHTTPD httpd = uriResource.initParameter(INIT_PARAM_INDEX_EMBEDDEDHTTPD,
                EmbeddedHTTPD.class);

        ContainerFileEntryWithContainerFile containerFileEntry = UmAppDatabase
                .getInstance(getContext(uriResource))
                .getContainerFileEntryDao().findContainerFileEntryWithContainerFileByEntryId(entryId);

        if(containerFileEntry == null) {
            //this container does not exist here anymore
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                    "text/plain", "Container " + entryId + " not found by catalog controller");
        }

        File containerFile = new File(containerFileEntry.getContainerFile().getNormalizedPath());

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

    }

    public NanoHTTPD.Response handleContainerEntryResponse(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.Method method, NanoHTTPD.IHTTPSession session, String normalizedUri){
//        TODO: re-implement this for when we are using this for serving contents over HTTP e.g. for the GWT client
        //serve a particular file from the container
//        String pathInZip = normalizedUri.substring(containerIdRange[1] + 1);
//        WeakHashMap zipMap = uriResource.initParameter(INIT_PARAM_INDEX_HASHMAP, WeakHashMap.class);
//        ZipFile zipFile;
//        if(zipMap.containsKey(containerFileEntry.getContainerFile().getNormalizedPath())) {
//            zipFile = (ZipFile)zipMap.get(containerFileEntry.getContainerFile().getNormalizedPath());
//        }else {
//            if(!containerFile.exists()) {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
//                        "text/plain","Not found: " + normalizedUri);
//            }
//
//            try {
//                zipFile = new ZipFile(containerFile);
//                zipMap.put(containerFileEntry.getContainerFile().getNormalizedPath(), zipFile);
//            }catch(ZipException e) {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR,
//                        "text/plain", e.toString());
//            }
//
//        }
//
//        return newResponseFromFile(method, uriResource, session, new ZipEntrySource(zipFile, pathInZip));
        return null;

    }

    public NanoHTTPD.Response handleEntryStatusRequest(RouterNanoHTTPD.UriResource uriResource,
                                                       NanoHTTPD.Method method,
                                                       NanoHTTPD.IHTTPSession session,
                                                       String normalizedUri) throws IOException, NanoHTTPD.ResponseException {

        Object context = getContext(uriResource);
        HashMap<String, String> files = new HashMap<>();
        session.parseBody(files);
        String jsonRequest = session.getQueryParameterString();
        JSONObject requestJsonObj = new JSONObject(jsonRequest);
        JSONArray requestEntryIds = requestJsonObj.getJSONArray(EntryStatusTask.ENTRY_RESPONSE_ENTRIES_KEY);

        String[] entryIdList = new String[requestEntryIds.length()];
        for(int i = 0; i < requestEntryIds.length(); i++) {
            entryIdList[i] = requestEntryIds.getString(i);
        }

        List<ContainerFileEntry> containerFileEntries = UmAppDatabase
                .getInstance(context).getContainerFileEntryDao()
                .findContainerFileEntriesByEntryIds(entryIdList);
        Gson gson = new Gson();
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
                gson.toJson(containerFileEntries));
    }

    public NanoHTTPD.Response handleContainerFileRequest(RouterNanoHTTPD.UriResource uriResource, NanoHTTPD.Method method, NanoHTTPD.IHTTPSession session) throws IOException {
        return handleContainerFileRequest(uriResource, method, session, RouterNanoHTTPD.normalizeUri(session.getUri()));
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
//        if(containerIdEnd == -1)
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
                    return handleContainerFileRequest(uriResource, NanoHTTPD.Method.HEAD, session);
                }
            }
        }catch(IOException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "text/plain",
                    "Exception:"  + e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "text/plain",
                "Request not understood by .other method: " + method);
    }

    /**
     *
     * @param str
     * @return
     */
    public static String doubleUrlEncode(String str) {
        try {
            return URLEncoder.encode(URLEncoder.encode(str, "UTF-8"), "UTF-8");
        }catch(IOException e) {
            e.printStackTrace();//can only occur on a system that does not support utf8
        }

        return null;
    }
}
