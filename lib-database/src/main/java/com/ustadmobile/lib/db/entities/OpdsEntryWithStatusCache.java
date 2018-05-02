package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

/**
 * Represents an OpdsEntry joined to it's related OpdsEntryStatusCache (created using a JOIN query).
 *
 */
public class OpdsEntryWithStatusCache extends OpdsEntryWithRelations {

    @UmEmbedded
    OpdsEntryStatusCache statusCache;

    @UmEmbedded
    DownloadJobItem pendingDownloadJobItem;

    /**
     * Setter for status cache property.
     *
     * @return The associated OpdsEntryStatusCache object (
     */
    public OpdsEntryStatusCache getStatusCache() {
        return statusCache;
    }

    public void setStatusCache(OpdsEntryStatusCache statusCache) {
        this.statusCache = statusCache;
    }

    public DownloadJobItem getPendingDownloadJobItem() {
        return pendingDownloadJobItem;
    }

    public void setPendingDownloadJobItem(DownloadJobItem pendingDownloadJobItem) {
        this.pendingDownloadJobItem = pendingDownloadJobItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpdsEntryWithStatusCache)) return false;
        if (!super.equals(o)) return false;

        OpdsEntryWithStatusCache that = (OpdsEntryWithStatusCache) o;

        return statusCache != null ? statusCache.equals(that.statusCache) : that.statusCache == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (statusCache != null ? statusCache.hashCode() : 0);
        return result;
    }
}
