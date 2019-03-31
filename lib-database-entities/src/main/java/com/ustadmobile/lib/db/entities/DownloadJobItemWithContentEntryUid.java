package com.ustadmobile.lib.db.entities;

@Deprecated
public class DownloadJobItemWithContentEntryUid extends DownloadJobItem {


    private long djiContentEntryUid;

    public DownloadJobItemWithContentEntryUid() {

    }

    public DownloadJobItemWithContentEntryUid(DownloadJobItemWithContentEntryUid src) {
        setDjiContentEntryUid(src.getDjiContentEntryUid());
        setDestinationFile(src.getDestinationFile());
        setDjiContainerUid(src.getDjiContainerUid());
        setDjiDjUid(src.getDjiDjUid());
        setDjiStatus(src.getDjiStatus());
        setCurrentSpeed(src.getCurrentSpeed());
        setDownloadedSoFar(src.getDownloadedSoFar());
        setDownloadLength(src.getDownloadLength());
        setNumAttempts(src.getNumAttempts());
        setTimeFinished(src.getTimeFinished());
        setTimeStarted(src.getTimeStarted());
    }

    public DownloadJobItemWithContentEntryUid(long djiContentEntryUid, long downloadLength) {
        this.djiContentEntryUid = djiContentEntryUid;
        setDownloadLength(downloadLength);
    }

    @Override
    public long getDjiContentEntryUid() {
        return djiContentEntryUid;
    }

    public void setDjiContentEntryUid(long djiContentEntryUid) {
        this.djiContentEntryUid = djiContentEntryUid;
    }
}
