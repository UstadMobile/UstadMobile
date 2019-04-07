package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContentEntryStatus {


    public static final int LOCAL_STATUS_UNAVAILABLE = 0;

    public static final int LOCAL_STATUS_AVAILABLE = 1;

    //Always equals contentEntryUid
    @UmPrimaryKey
    private long cesUid;

    private long totalSize;

    private long bytesDownloadSoFar;

    private int downloadStatus;

    private boolean locallyAvailable;

    private int downloadSpeed;

    private boolean invalidated = false;

    private boolean cesLeaf;

    public ContentEntryStatus() {

    }

    public ContentEntryStatus(long contentEntryUid, boolean isLeaf, long totalSize) {
        this.cesUid = contentEntryUid;
        this.cesLeaf = isLeaf;
        this.totalSize = totalSize;
    }

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

    public boolean isCesLeaf() {
        return cesLeaf;
    }

    public void setCesLeaf(boolean cesLeaf) {
        this.cesLeaf = cesLeaf;
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


    public boolean isLocallyAvailable() {
        return locallyAvailable;
    }

    public void setLocallyAvailable(boolean locallyAvailable) {
        this.locallyAvailable = locallyAvailable;
    }

    public int getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
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
        return cesLeaf == that.cesLeaf;
    }

    @Override
    public int hashCode() {
        int result = (int) (cesUid ^ (cesUid >>> 32));
        result = 31 * result + (int) (totalSize ^ (totalSize >>> 32));
        result = 31 * result + (int) (bytesDownloadSoFar ^ (bytesDownloadSoFar >>> 32));
        result = 31 * result + downloadStatus;
        result = 31 * result + (invalidated ? 1 : 0);
        result = 31 * result + (cesLeaf ? 1 : 0);
        return result;
    }
}
