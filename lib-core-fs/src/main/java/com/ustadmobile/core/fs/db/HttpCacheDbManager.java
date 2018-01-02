package com.ustadmobile.core.fs.db;

/**
 * CacheDbManager manages the persistence of cache db ORM objects.
 */
public abstract class HttpCacheDbManager  {

    private static HttpCacheDbManager instance;

    /**
     * Gets the singleton instance
     *
     * @return Singleton instance of HttpCacheDbManager
     */
    public static HttpCacheDbManager getInstance() {
        if(instance == null)
            instance = HttpCacheDbManagerFactory.makeHttpCacheDbManager();

        return instance;
    }

    /**
     * Find a cache entry by URL
     *
     * @param context System context object
     * @param url URL of the entry to lookup
     *
     * @return HttpCacheDbEntry object representing that entry, or null if not found
     */
    public abstract HttpCacheDbEntry getEntryByUrl(Object context, String url);

    /**
     * Create a new cache db entry object
     *
     * @param context System context object
     *
     * @return New HttpCacheDbEnrty object implementation
     */
    public abstract HttpCacheDbEntry makeNewEntry(Object context);

    /**
     * Persist (create or update) the HttpCacheDbEntry object provided
     *
     * @param context System context object
     * @param entry Entry to persist
     */
    public abstract void persist(Object context, HttpCacheDbEntry entry);


    /**
     * Delete an entry from the database
     *
     * @param context
     * @param entry
     */
    public abstract void delete(Object context, HttpCacheDbEntry entry);

}
