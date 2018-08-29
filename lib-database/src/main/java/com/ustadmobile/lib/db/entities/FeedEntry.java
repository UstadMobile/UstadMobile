package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class FeedEntry implements SyncableEntity{

    @UmPrimaryKey(autoIncrement = true)
    private long feedEntryUid;

    private long feedEntryPersonUid;

    private String title;

    private String description;

    private String link;

    private long deadline;

    private long feedEntryHash;

    public long getFeedEntryUid() {
        return feedEntryUid;
    }

    public void setFeedEntryUid(long feedEntryUid) {
        this.feedEntryUid = feedEntryUid;
    }

    public long getFeedEntryPersonUid() {
        return feedEntryPersonUid;
    }

    public void setFeedEntryPersonUid(long feedEntryPersonUid) {
        this.feedEntryPersonUid = feedEntryPersonUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Get the deadline (in utime) by which the entry should be actioned (or 0 for no deadline)
     *
     * @return the deadline (in utime) by which the entry should be actioned (or 0 for no deadline)
     */
    public long getDeadline() {
        return deadline;
    }

    /**
     * Set the deadline (in utime) by which the entry should be actioned (or 0 for no deadline)
     *
     * @param deadline the deadline (in utime) by which the entry should be actioned (or 0 for no deadline)
     */
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    /**
     * Get the feed entry hash. Feed entries may be generated simultaneously on multiple devices.
     * The same feed entry must have the same feedEntryHash - e.g. to take attendance for a given class
     * for a given day.
     *
     * @return the feed entry hash as above
     */
    public long getFeedEntryHash() {
        return feedEntryHash;
    }

    /**
     * Set the feed entry hash
     *
     * @see #getFeedEntryHash()
     *
     * @param feedEntryHash the feed entry hash
     */
    public void setFeedEntryHash(long feedEntryHash) {
        this.feedEntryHash = feedEntryHash;
    }

    @Override
    public long getMasterChangeSeqNum() {
        return 0;
    }

    @Override
    public void setMasterChangeSeqNum(long masterChangeSeqNum) {

    }

    @Override
    public long getLocalChangeSeqNum() {
        return 0;
    }

    @Override
    public void setLocalChangeSeqNum(long localChangeSeqNum) {

    }
}
