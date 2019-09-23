package com.ustadmobile.core.networkmanager

import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import kotlin.js.JsName

/**
 * Interface for core presenters to access download status related methods (implemented in
 * NetworkManagerBle)
 */
interface DownloadJobItemStatusProvider {

    /**
     * Find the current download status of a given contententry that is part of an active download
     * job
     *
     * @param contentEntryUid Content Entry UID of the item to find
     * @param callback Callback that will receive the status. The callback value will be null if the
     * given contententry is not part of any active download job.
     */
    @JsName("findDownloadJobItemStatusByContentEntryUid")
    suspend fun findDownloadJobItemStatusByContentEntryUid(contentEntryUid: Long) : DownloadJobItemStatus?

    /**
     * Add a listener to receive events when downloadstatus changes.
     *
     * @param listener listener to add
     */
    @JsName("addDownloadChangeListener")
    fun addDownloadChangeListener(listener: OnDownloadJobItemChangeListener)

    /**
     * Remove a listener that was previously added
     *
     * @param listener listener to remove
     */
    @JsName("removeDownloadChangeListener")
    fun removeDownloadChangeListener(listener: OnDownloadJobItemChangeListener)


}
