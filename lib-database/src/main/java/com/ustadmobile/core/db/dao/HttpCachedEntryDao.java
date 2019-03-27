package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;

import java.util.List;

/**
 * DAO for managing HttpCachedEntry database entries, used for running the http cache database
 */
@UmDao
public abstract class HttpCachedEntryDao {

    /**
     * Find an entry given the url and HTTP method
     *
     * @see HttpCachedEntry#METHOD_GET
     * @see HttpCachedEntry#METHOD_HEAD
     * @see HttpCachedEntry#METHOD_POST
     *
     * @param url URL of the entry retrieved
     * @param method HTTP Method - flag as per HttpCachedEntry#METHOD_ flags
     *
     * @return The HttpCachedEntry representing the given entry, null if it's not in the database
     */
    @UmQuery("SELECT * FROM HttpCachedEntry WHERE url = :url AND method = :method")
    public abstract HttpCachedEntry findByUrlAndMethod(String url, int method);

    /**
     * Update method - updateState the given entry. Has no effect if it has not already been inserted
     *
     * @param entry Entry to updateState
     */
    @UmUpdate
    public abstract void update(HttpCachedEntry entry);

    /**
     * Insert - will replace if the entry already exists
     *
     * @param entry Entry to insert or replace
     */
    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insert(HttpCachedEntry entry);

    /**
     * Get a list of the file uris for a given list of urls. Useful when it's time to deleteByDownloadSetUid the entries from the disk.
     *
     * @param urls URLs to find entries for
     *
     * @return A list of Strings of all known file paths for the given urls
     */
    @UmQuery("SELECT fileUri FROM HttpCachedEntry WHERE url in (:urls)")
    public abstract List<String> findFileUrisByUrl(List<String> urls);

    /**
     * Delete HttpCachedEntry objects by their file path. Useful when entries have been deleted from disk.
     *
     * @param deletedFileUris
     */
    @UmQuery("DELETE FROM HttpCachedEntry WHERE fileUri in (:deletedFileUris)")
    public abstract void deleteByFileUris(List<String> deletedFileUris);


}
