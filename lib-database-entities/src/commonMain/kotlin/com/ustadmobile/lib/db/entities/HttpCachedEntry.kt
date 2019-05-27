package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey


/**
 * Entity representing an HTTP cache entry
 *
 */
@UmEntity
@Entity
class HttpCachedEntry() {

    /**
     * Primary (artificial) key
     *
     * @return primary key
     */
    /**
     * Set the primary key - only to be used by the ORM
     * @param uid
     */
    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null

    /**
     * Get the URL that this cache object represents
     *
     * @return The URL this cache object represents
     */
    /**
     * Set the URL the cache object represents.
     *
     * @param url The URL this cache objects represents
     */
    @UmIndexField
    var url: String? = null

    /**
     * The file URI where the data of this entry is stored
     *
     * @return The file URI where the data of this entry is stored
     */
    /**
     * Set the file URI where the data of this entry is stored
     * @param fileUri
     */
    var fileUri: String? = null

    /**
     * The HTTP method that this entry represents. The cache response varies by HTTP method. Use the
     * METHOD_ flags
     *
     * @return The method used to retrieve this cache response.
     */
    /**
     * Set the HTTP method that this entry represents.
     *
     * @param method The HTTP method that this cached entry represents
     */
    var method: Int = 0

    /**
     * The calculated expires time of this entry (in unix time)
     *
     * @return The calculated expires time of this entry
     */
    /**
     * Set the calculated expires time of this entry
     *
     * @param expiresTime The calculated expires time for this entry
     */
    var expiresTime: Long = -1

    /**
     * The content type from the HTTP response
     *
     * @return The HTTP content type header value
     */
    /**
     * Set the http content header value
     *
     * @param contentType The HTTP content type
     */
    var contentType: String? = null

    /**
     * Getter for the etag
     *
     * @return The HTTP ETAG header (if known)
     */
    /**
     * Setter for the etag property
     *
     * @param etag The HTTP ETAG header (null if not present)
     */
    var etag: String? = null

    /**
     * Getter for the last modified property
     *
     * @return The last modified time (parsed from the HTTP last-modified header)
     */
    /**
     * Setter for the last modified property
     *
     * @param lastModified The last modified time (parsed from the HTTP last-modified header)
     */
    var lastModified: Long = 0

    /**
     * Getter for the cache control property
     *
     * @return The cache-control header as it was provided by the server, null if none was provided
     */
    /**
     * Setter for the cache control property
     *
     * @param cacheControl The cache-control header as it was provided by the server, null if none was provided
     */
    var cacheControl: String? = null

    /**
     * Getter for the content length property
     *
     * @return The content-length header as it was provided by the server
     */
    /**
     * Setter for the content length property
     *
     * @param contentLength The content-length header as it was provided by the server
     */
    var contentLength: Long = 0

    /**
     * Getter for the status code property
     *
     * @return The HTTP status code as it was provided by the server.
     */
    /**
     * Setter for the status code property
     *
     * @param statusCode The HTTP status code as it was provided by the server.
     */
    var statusCode: Int = 0

    /**
     * Getter for the last checked property
     *
     * @return The time, in unix time, that this entry was last checked against the server
     */
    /**
     * Setter for the last checked property
     *
     * @param lastChecked The time, in unix time, that this entry was last checked against the server
     */
    var lastChecked: Long = 0

    /**
     * Getter for the last accessed property
     *
     * @return The time that this entry was last accessed in a request (used to prioritize retention / deletion)
     */
    /**
     * Setter for the last accessed property
     *
     * @param lastAccessed The time that this entry was last accessed in a request (used to prioritize retention / deletion)
     */
    @UmIndexField
    var lastAccessed: Long = 0

    companion object {

        const val METHOD_GET = 0

        const val METHOD_POST = 1

        const val METHOD_HEAD = 2


        internal val CACHE_CONTROL_KEY_MAX_AGE = "max-age"

        /**
         * The default time for which a cache entry is considered fresh from the time it was last checked
         * if the server does not provide this information using the cache-control or expires header.
         */
        const val DEFAULT_TIME_TO_LIVE = 60 * 60 * 1000
    }
}
