package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContentEntryStatus {

    @UmPrimaryKey
    private long cesUid;

    @UmIndexField
    private long cesContentEntryUid;

    private long entryBytesDownloadedSoFar;

    private long bytesDownloadSoFarIncDescendants;

    private long sizeIncDescendants;

    private long entrySize;

    private int entriesWithContainerIncDescendants;

    private int entryHasContainer;

    private int containersDownloadedIncDescendants;

    private int entryContainerDownloaded;

    private int containersDownloadPendingIncAncestors;

    private int entryContainerDownloadPending;

    private long entryAcquisitionLinkLength;

    private int activeDownloadsIncAncestors;

    private int entryActiveDownload;

    private int pausedDownloadsIncAncestors;

    private int entryPausedDownload;

    private boolean invalidated = false;

    public long getCesUid() {
        return cesUid;
    }

    public void setCesUid(long cesUid) {
        this.cesUid = cesUid;
    }

    public long getCesContentEntryUid() {
        return cesContentEntryUid;
    }

    public void setCesContentEntryUid(long cesContentEntryUid) {
        this.cesContentEntryUid = cesContentEntryUid;
    }

    public long getEntryBytesDownloadedSoFar() {
        return entryBytesDownloadedSoFar;
    }

    public void setEntryBytesDownloadedSoFar(long entryBytesDownloadedSoFar) {
        this.entryBytesDownloadedSoFar = entryBytesDownloadedSoFar;
    }

    public long getBytesDownloadSoFarIncDescendants() {
        return bytesDownloadSoFarIncDescendants;
    }

    public void setBytesDownloadSoFarIncDescendants(long bytesDownloadSoFarIncDescendants) {
        this.bytesDownloadSoFarIncDescendants = bytesDownloadSoFarIncDescendants;
    }

    public long getSizeIncDescendants() {
        return sizeIncDescendants;
    }

    public void setSizeIncDescendants(long sizeIncDescendants) {
        this.sizeIncDescendants = sizeIncDescendants;
    }

    public long getEntrySize() {
        return entrySize;
    }

    public void setEntrySize(long entrySize) {
        this.entrySize = entrySize;
    }

    public int getEntriesWithContainerIncDescendants() {
        return entriesWithContainerIncDescendants;
    }

    public void setEntriesWithContainerIncDescendants(int entriesWithContainerIncDescendants) {
        this.entriesWithContainerIncDescendants = entriesWithContainerIncDescendants;
    }

    public int getEntryHasContainer() {
        return entryHasContainer;
    }

    public void setEntryHasContainer(int entryHasContainer) {
        this.entryHasContainer = entryHasContainer;
    }

    public int getContainersDownloadedIncDescendants() {
        return containersDownloadedIncDescendants;
    }

    public void setContainersDownloadedIncDescendants(int containersDownloadedIncDescendants) {
        this.containersDownloadedIncDescendants = containersDownloadedIncDescendants;
    }

    public int getEntryContainerDownloaded() {
        return entryContainerDownloaded;
    }

    public void setEntryContainerDownloaded(int entryContainerDownloaded) {
        this.entryContainerDownloaded = entryContainerDownloaded;
    }

    public int getContainersDownloadPendingIncAncestors() {
        return containersDownloadPendingIncAncestors;
    }

    public void setContainersDownloadPendingIncAncestors(int containersDownloadPendingIncAncestors) {
        this.containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors;
    }

    public int getEntryContainerDownloadPending() {
        return entryContainerDownloadPending;
    }

    public void setEntryContainerDownloadPending(int entryContainerDownloadPending) {
        this.entryContainerDownloadPending = entryContainerDownloadPending;
    }

    public long getEntryAcquisitionLinkLength() {
        return entryAcquisitionLinkLength;
    }

    public void setEntryAcquisitionLinkLength(long entryAcquisitionLinkLength) {
        this.entryAcquisitionLinkLength = entryAcquisitionLinkLength;
    }

    public int getActiveDownloadsIncAncestors() {
        return activeDownloadsIncAncestors;
    }

    public void setActiveDownloadsIncAncestors(int activeDownloadsIncAncestors) {
        this.activeDownloadsIncAncestors = activeDownloadsIncAncestors;
    }

    public int getEntryActiveDownload() {
        return entryActiveDownload;
    }

    public void setEntryActiveDownload(int entryActiveDownload) {
        this.entryActiveDownload = entryActiveDownload;
    }

    public int getPausedDownloadsIncAncestors() {
        return pausedDownloadsIncAncestors;
    }

    public void setPausedDownloadsIncAncestors(int pausedDownloadsIncAncestors) {
        this.pausedDownloadsIncAncestors = pausedDownloadsIncAncestors;
    }

    public int getEntryPausedDownload() {
        return entryPausedDownload;
    }

    public void setEntryPausedDownload(int entryPausedDownload) {
        this.entryPausedDownload = entryPausedDownload;
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }
}
