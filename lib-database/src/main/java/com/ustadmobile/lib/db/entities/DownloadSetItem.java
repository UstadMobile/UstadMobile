package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents an item (linked to an OPDS Entry) which is part of a specific DownloadSet.
 */
@UmEntity
public class DownloadSetItem {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    @UmIndexField
    private int downloadSetId;

    @UmIndexField
    private String opdsEntryUuid;

    @UmIndexField
    private String entryId;

    private long updated;

    private int containerFileId;

    public DownloadSetItem() {

    }

    /**
     * Constructor
     *
     * @param entry The OPDS Entry from which this entry is downloaded (e.g. the remote entry with an
     *              acquisition link - not the entry from it's container on disk)
     * @param set The DownloadSet this item is part of
     */
    public DownloadSetItem(OpdsEntryWithRelations entry, DownloadSet set) {
        this(entry, set.getId());
    }

    /**
     * Constructor
     *
     * @param entry The OPDS Entry from which this entry is downloaded (e.g. the remote entry with an
     *              acquisition link - not the entry from it's container on disk)
     * @param downloadSetId The primary key of the DownloadSet that this item is part of
     */
    public DownloadSetItem(OpdsEntryWithRelations entry, int downloadSetId) {
        this.downloadSetId = downloadSetId;
        this.entryId = entry.getEntryId();
        this.opdsEntryUuid = entry.getUuid();

    }

    /**
     * Get the primary key value
     *
     * @return the primary key value
     */
    public int getId() {
        return id;
    }

    /**
     * Set the primary key value
     * @param id the primary key value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the primary key of the DownloadSet that this item is part of
     *
     * @return the primary key of the DownloadSet that this item is part of
     */
    public int getDownloadSetId() {
        return downloadSetId;
    }

    /**
     * Set the primary key of the DownloadSet that this item is part of
     *
     * @param downloadSetId the primary key of the DownloadSet that this item is part of
     */
    public void setDownloadSetId(int downloadSetId) {
        this.downloadSetId = downloadSetId;
    }

    /**
     * Get the OPDS entryId for this DownloadSetItem
     *
     * @return the OPDS entryId for this DownloadSetItem
     */
    public String getEntryId() {
        return entryId;
    }

    /**
     * Set the OPDS entryId for this DownloadSetItem
     *
     * @param entryId the OPDS entryId for this DownloadSetItem
     */
    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    /**
     * Get the time (in ms) that this entry was last updated. This is as per the updated
     * Atom element. E.g. if the updated time is after the time that this item was last downloaded
     * to disk, then the item should be updated.
     *
     * @return the time (in ms) that this entry was last updated (in ms)
     */
    public long getUpdated() {
        return updated;
    }

    /**
     * Set the time (in ms) that this entry was last updated. This is as per the updated
     * Atom element. E.g. if the updated time is after the time that this item was last downloaded
     * to disk, then the item should be updated.
     *
     * @param updated the time (in ms) that this entry was last updated (in ms)
     */
    public void setUpdated(long updated) {
        this.updated = updated;
    }

    /**
     * Get the primary key of the associated ContainerFileEntry (or 0 if unset)
     *
     * @return the primary key of the associated ContainerFileEntry
     */
    public int getContainerFileId() {
        return containerFileId;
    }

    /**
     * Set the primary key of the associated ContainerFileEntry (or 0 if unset)
     * @param containerFileId the primary key of the associated ContainerFileEntry (or 0 if unset)
     */
    public void setContainerFileId(int containerFileId) {
        this.containerFileId = containerFileId;
    }

    /**
     * Get the uuid of the related OPDS entry (from which this entry is downloaded (e.g. the remote
     * entry with an acquisition link - not the entry from it's container on disk)
     *
     * @return the uuid of the related OPDS entry
     */
    public String getOpdsEntryUuid() {
        return opdsEntryUuid;
    }

    /**
     * Set the uuid of the related OPDS entry (from which this entry is downloaded (e.g. the remote
     * entry with an acquisition link - not the entry from it's container on disk)
     *
     * @param opdsEntryUuid the uuid of the related OPDS entry
     */
    public void setOpdsEntryUuid(String opdsEntryUuid) {
        this.opdsEntryUuid = opdsEntryUuid;
    }

}
