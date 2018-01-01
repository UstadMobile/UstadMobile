package com.ustadmobile.core.fs.db;

/**
 * This class MUST be overriden in the implementation being used. It is excluded from the jar by
 * design.
 */
public class HttpCacheDbManagerFactory {

    public static HttpCacheDbManager makeHttpCacheDbManager() {
        throw new RuntimeException("lib-core-fs2: HttpCacheDbManager factory must be used from implementation!");
    }
}
