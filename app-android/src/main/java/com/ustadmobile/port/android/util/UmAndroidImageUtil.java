package com.ustadmobile.port.android.util;

import android.widget.ImageView;

import com.pixplicity.sharp.Sharp;
import com.ustadmobile.core.impl.HttpCache;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.UstadMobileSystemImplFs;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;

import java.io.IOException;

/**
 * Created by mike on 2/16/18.
 */

public class UmAndroidImageUtil {


    public static boolean isSvg(String mimeType) {
        if(mimeType == null)
            return false;

        String mimeTypeLower = mimeType.toLowerCase();
        if(mimeTypeLower.startsWith("image/svg"))
            return true;
        else
            return false;
    }

    public static void loadSvgIntoImageView(String url, final ImageView imageView) {
        UmHttpRequest request = new UmHttpRequest(imageView.getContext(), url);
        HttpCache cache = ((UstadMobileSystemImplFs) UstadMobileSystemImpl.getInstance())
                .getHttpCache(imageView.getContext());
        cache.get(request, new UmHttpResponseCallback() {
            @Override
            public void onComplete(UmHttpCall call, UmHttpResponse response) {
                try {
                    Sharp.loadInputStream(response.getResponseAsStream()).into(imageView);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(UmHttpCall call, IOException exception) {

            }
        });
    }

}
