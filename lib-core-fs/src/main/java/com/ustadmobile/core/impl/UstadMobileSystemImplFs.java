package com.ustadmobile.core.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 12/27/17.
 */

public interface UstadMobileSystemImplFs {

    HttpCache getHttpCache(Object context);

    /**
     * Get an asset Synchronously
     * @param context
     * @param path
     * @return
     */
    InputStream getAssetSync(Object context, String path) throws IOException;

}
