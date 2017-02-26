package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSFeed;
import com.ustadmobile.core.opds.UstadJSOPDSItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

    public static final String ENTRY_PATH_COMPONENT = "/container/";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String normalizedUri = RouterNanoHTTPD.normalizeUri(session.getUri());

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Object context = uriResource.initParameter(0, Object.class);
        try {
            if(normalizedUri.endsWith("acquire.opds")) {
                UstadJSOPDSFeed deviceFeed = CatalogController.makeDeviceFeed(
                    impl.getStorageDirs(CatalogController.SHARED_RESOURCE, context),
                    CatalogController.SHARED_RESOURCE, "/catalog/entry/", CatalogController.LINK_HREF_MODE_ID,
                    context);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                deviceFeed.serialize(bout);
                bout.flush();
                ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                NanoHTTPD.Response r = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadJSOPDSItem.TYPE_ACQUISITIONFEED, bin, bout.size());
                return r;
            }else if(normalizedUri.contains(ENTRY_PATH_COMPONENT)) {
                int containerIdStart = normalizedUri.indexOf(ENTRY_PATH_COMPONENT)
                        + ENTRY_PATH_COMPONENT.length();
                int containerIdEnd = normalizedUri.indexOf('/', containerIdStart);
                if(containerIdEnd == -1)
                    containerIdEnd = normalizedUri.length();

                String uuid = normalizedUri.substring(containerIdStart, containerIdEnd);
                CatalogEntryInfo info = CatalogController.getEntryInfo(uuid, CatalogController.SHARED_RESOURCE,
                        context);
                if(info == null) {
                    //this container does not exist here anymore
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                            "text/plain", "Container " + uuid + " not found by catalog controller");
                }

                File containerFile = new File(info.fileURI);
                if(containerIdEnd == normalizedUri.length()) {
                    //this is the end of the path : serve the container itself
                    return newResponseFromFile(uriResource, session, new FileSource(containerFile));
                }else {
                    //serve a particular file from the container
                    String pathInZip = normalizedUri.substring(containerIdEnd + 1);
                    WeakHashMap zipMap = uriResource.initParameter(1, WeakHashMap.class);
                    ZipFile zipFile;
                    if(zipMap.containsKey(info.fileURI)) {
                        zipFile = (ZipFile)zipMap.get(info.fileURI);
                    }else {
                        zipFile = new ZipFile(containerFile);
                        zipMap.put(info.fileURI, zipFile);
                    }

                    ZipEntry fileEntry = zipFile.getEntry(pathInZip);
                    return newResponseFromFile(uriResource, session, new ZipEntrySource(fileEntry, zipFile));
                }
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
