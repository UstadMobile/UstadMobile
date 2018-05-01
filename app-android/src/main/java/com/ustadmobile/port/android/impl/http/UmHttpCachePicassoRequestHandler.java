package com.ustadmobile.port.android.impl.http;

import android.content.Context;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;
import com.ustadmobile.core.impl.AbstractCacheResponse;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Put Picasso requests through our network cache (allowing us to prime the cache, and ensure that
 * certain entries remain in the cache)
 *
 * Created by mike on 2/15/18.
 */

public class UmHttpCachePicassoRequestHandler extends RequestHandler{

    public static final String SCHEME_PREFIX = "um-";

    //Required for accessing the http cache as it uses the database

    private Context appContext;

    public UmHttpCachePicassoRequestHandler(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return data.uri.getScheme() != null && data.uri.getScheme().startsWith(SCHEME_PREFIX);
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        String uriStr = request.uri.toString().substring(SCHEME_PREFIX.length());


        UmHttpRequest umRequest = new UmHttpRequest(appContext, uriStr);
        if(NetworkPolicy.isOfflineOnly(networkPolicy)){
            umRequest.setOnlyIfCached(true);
        }

        UmHttpResponse response = UstadMobileSystemImpl.getInstance().makeRequestSync(umRequest);
        Picasso.LoadedFrom loadedFrom = Picasso.LoadedFrom.NETWORK;
        if(uriStr.startsWith("file:/")){
            loadedFrom = Picasso.LoadedFrom.DISK;
        }else if(response instanceof AbstractCacheResponse) {
            loadedFrom = ((AbstractCacheResponse)response).isHit() ? Picasso.LoadedFrom.DISK :
                    Picasso.LoadedFrom.NETWORK;
        }

        InputStream inputStream = response.getResponseAsStream();

        return new Result(inputStream, loadedFrom);
    }
}
