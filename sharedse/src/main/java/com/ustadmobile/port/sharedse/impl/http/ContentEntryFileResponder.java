package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.lib.db.entities.ContentEntryFileWithStatus;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * NanoHTTPD responder that will serve the content of a ContentEntryFile
 *
 * Initialization parameters required:
 *
 * 0: Context object
 *
 * This expects urls ending with /fileentryuid
 * e.g. /ContentEntryFile/1234
 *
 * Use with NanoHTTPD Router like so:
 *
 * addRoute("/ContentEntryFile/(.*)+", ContentEntryFileResponder.class, context);
 *
 */
@Deprecated
public class ContentEntryFileResponder extends FileResponder implements RouterNanoHTTPD.UriResponder {

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Object context = uriResource.initParameter(0, Object.class);
        String url = RouterNanoHTTPD.normalizeUri(session.getUri());
        int lastSlashPos = url.lastIndexOf('/');
        if(lastSlashPos == -1) {
            return null;//TODO:Bad request
        }

        String entryFileUid = url.substring(lastSlashPos + 1);
        try {
            UmAppDatabase dbRepo = UmAccountManager.getRepositoryForActiveAccount(context);
            ContentEntryFileWithStatus entryFile = dbRepo.getContentEntryFileDao()
                    .findByUidWithStatus(Long.parseLong(entryFileUid));
            if(entryFile.getEntryStatus() == null || entryFile.getEntryStatus().getFilePath() == null) {
                //todo: return 404
                return null;
            }

            File file = new File(entryFile.getEntryStatus().getFilePath());
            return newResponseFromFile(uriResource, session, new FileSource(file));
        }catch(NumberFormatException ne){

            return null;
            //todo: return bad request
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
