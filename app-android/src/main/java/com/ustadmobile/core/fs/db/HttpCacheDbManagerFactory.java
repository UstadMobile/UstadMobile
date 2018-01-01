package com.ustadmobile.core.fs.db;

import com.ustadmobile.port.android.fs.cachedb.HttpCacheDbEntryManagerAndroid;

/**
 * Created by mike on 12/30/17.
 */

public class HttpCacheDbManagerFactory {

    public static HttpCacheDbManager makeHttpCacheDbManager() {
        return new HttpCacheDbEntryManagerAndroid();
    }
}
