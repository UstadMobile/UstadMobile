package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContentEntryStatus {

    //Always equals contentEntryUid
    @UmPrimaryKey
    private long cesUid;

    private long totalSize;

    private long bytesDownloadSoFar;

    private int downloadStatus;

    private boolean invalidated = false;

    private boolean leaf;

    public long getCesUid() {
        return cesUid;
    }

    public void setCesUid(long cesUid) {
        this.cesUid = cesUid;
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getBytesDownloadSoFar() {
        return bytesDownloadSoFar;
    }

    public void setBytesDownloadSoFar(long bytesDownloadSoFar) {
        this.bytesDownloadSoFar = bytesDownloadSoFar;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntryStatus that = (ContentEntryStatus) o;

        if (cesUid != that.cesUid) return false;
        if (totalSize != that.totalSize) return false;
        if (bytesDownloadSoFar != that.bytesDownloadSoFar) return false;
        if (downloadStatus != that.downloadStatus) return false;
        if (invalidated != that.invalidated) return false;
        return leaf == that.leaf;
    }

    @Override
    public int hashCode() {
        int result = (int) (cesUid ^ (cesUid >>> 32));
        result = 31 * result + (int) (totalSize ^ (totalSize >>> 32));
        result = 31 * result + (int) (bytesDownloadSoFar ^ (bytesDownloadSoFar >>> 32));
        result = 31 * result + downloadStatus;
        result = 31 * result + (invalidated ? 1 : 0);
        result = 31 * result + (leaf ? 1 : 0);
        return result;
    }
}
