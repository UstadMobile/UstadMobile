package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.lib.db.entities.HttpCachedEntry

/**
 * DAO for managing HttpCachedEntry database entries, used for running the http cache database
 */
@Dao
abstract class HttpCachedEntryDao {

    /**
     * Find an entry given the url and HTTP method
     *
     * @see HttpCachedEntry.METHOD_GET
     *
     * @see HttpCachedEntry.METHOD_HEAD
     *
     * @see HttpCachedEntry.METHOD_POST
     *
     *
     * @param url URL of the entry retrieved
     * @param method HTTP Method - flag as per HttpCachedEntry#METHOD_ flags
     *
     * @return The HttpCachedEntry representing the given entry, null if it's not in the database
     */
    @Query("SELECT * FROM HttpCachedEntry WHERE url = :url AND method = :method")
    abstract fun findByUrlAndMethod(url: String, method: Int): HttpCachedEntry

    /**
     * Update method - updateStateAsync the given entry. Has no effect if it has not already been inserted
     *
     * @param entry Entry to updateStateAsync
     */
    @Update
    abstract fun update(entry: HttpCachedEntry)

    /**
     * Insert - will replace if the entry already exists
     *
     * @param entry Entry to insert or replace
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entry: HttpCachedEntry)

    /**
     * Get a list of the file uris for a given list of urls. Useful when it's time to deleteByDownloadSetUid the entries from the disk.
     *
     * @param urls URLs to find entries for
     *
     * @return A list of Strings of all known file paths for the given urls
     */
    @Query("SELECT fileUri FROM HttpCachedEntry WHERE url in (:urls)")
    abstract fun findFileUrisByUrl(urls: List<String>): List<String>

    /**
     * Delete HttpCachedEntry objects by their file path. Useful when entries have been deleted from disk.
     *
     * @param deletedFileUris
     */
    @Query("DELETE FROM HttpCachedEntry WHERE fileUri in (:deletedFileUris)")
    abstract fun deleteByFileUris(deletedFileUris: List<String>)


}
