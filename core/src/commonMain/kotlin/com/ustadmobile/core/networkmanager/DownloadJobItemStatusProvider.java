package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;

/**
 * Interface for core presenters to access download status related methods (implemented in
 * NetworkManagerBle)
 */
public interface DownloadJobItemStatusProvider {

    /**
     * Find the current download status of a given contententry that is part of an active download
     * job
     *
     * @param contentEntryUid Content Entry UID of the item to find
     * @param callback Callback that will receive the status. The callback value will be null if the
     *                 given contententry is not part of any active download job.
     */
    void findDownloadJobItemStatusByContentEntryUid(long contentEntryUid,
                                                           UmResultCallback<DownloadJobItemStatus> callback);

    /**
     * Add a listener to receive events when downloadstatus changes.
     *
     * @param listener listener to add
     */
    void addDownloadChangeListener(DownloadJobItemManager.OnDownloadJobItemChangeListener listener);

    /**
     * Remove a listener that was previously added
     *
     * @param listener listener to remove
     */
    void removeDownloadChangeListener(DownloadJobItemManager.OnDownloadJobItemChangeListener listener);


}
