package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents a set of entries that will be downloaded, with a single root entry. The DownloadSet
 * will often also include the descendant entries of the root entry (which are discovered by the
 * CrawlTask)
 *
 * <ol>
 * <li>
 *    A DownloadJob has a 1:many relationship with DownloadJobRun, which represents a download run of
 *    the set. It can be rerun (e.g. to update), which leads to a second DownloadJobRun entity.
 *   </li>
 *   <li>
 *       A DownloadJob has a 1:many relationship with DownloadJobItem, each of which represents a
 *       single entry in the download set. Each DownloadJobItem has a 1:many relationship with a
 *       DownloadJobItemRun
 *   </li>
 * </ol>
 */

@UmEntity
public class DownloadSet {

    @UmPrimaryKey(autoIncrement = true)
    private int dsUid;

    private String destinationDir;

    private boolean meteredNetworkAllowed = false;

    private long dsRootContentEntryUid;

    public DownloadSet(){

    }

    public int getDsUid() {
        return dsUid;
    }

    public void setDsUid(int dsUid) {
        this.dsUid = dsUid;
    }

    public String getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }

    public boolean isMeteredNetworkAllowed() {
        return meteredNetworkAllowed;
    }

    public void setMeteredNetworkAllowed(boolean meteredNetworkAllowed) {
        this.meteredNetworkAllowed = meteredNetworkAllowed;
    }

    public long getDsRootContentEntryUid() {
        return dsRootContentEntryUid;
    }

    public void setDsRootContentEntryUid(long dsRootContentEntryUid) {
        this.dsRootContentEntryUid = dsRootContentEntryUid;
    }
}
