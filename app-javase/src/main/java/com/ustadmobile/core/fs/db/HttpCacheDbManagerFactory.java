package com.ustadmobile.core.fs.db;

import com.ustadmobile.port.javase.fs.db.HttpCacheDbManagerJdbc;

/**
 * Created by mike on 12/31/17.
 */

public class HttpCacheDbManagerFactory {

    public static HttpCacheDbManager makeHttpCacheDbManager() {
        return new HttpCacheDbManagerJdbc();
    }
}
