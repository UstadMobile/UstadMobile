package com.ustadmobile.port.android.impl.http;


import android.content.Context;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

/**
 * Created by mike on 8/30/16.
 */
public class AndroidAssetsHandler implements RouterNanoHTTPD.UriResponder {

    public static final String URI_ROUTE_POSTFIX = "/(.)+";

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Context context = uriResource.initParameter(Context.class);
        String assetPath = RouterNanoHTTPD.normalizeUri(session.getUri()).substring(uriResource.getUri().length()-(URI_ROUTE_POSTFIX.length()-1));
        InputStream assetIn = null;
        NanoHTTPD.Response response = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            assetIn = context.getAssets().open(UMFileUtil.joinPaths(new String[]{"http", assetPath}));

            UMIOUtils.readFully(assetIn, bout, 1024);
            byte[] assetBytes = bout.toByteArray();
            String extension = UMFileUtil.getExtension(assetPath);

            response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadMobileSystemImpl.getInstance().getMimeTypeFromExtension(extension),
                    new ByteArrayInputStream(assetBytes), assetBytes.length);
            response.addHeader("Cache-Control", "cache, max-age=86400");
            response.addHeader("Content-Length", String.valueOf(assetBytes.length));
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 88, session.getUri(), e);
        }finally {
            try {
                if(assetIn != null) {
                    assetIn.close();
                }

            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 89, session.getUri(), e);
            }
        }

        return response;

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
