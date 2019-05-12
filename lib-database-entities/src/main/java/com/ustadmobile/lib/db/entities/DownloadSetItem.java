package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Represents an item (linked to a ContentEntryFile) which is part of a specific DownloadSet.
 */
@UmEntity
public class DownloadSetItem {

    @UmPrimaryKey(autoIncrement = true)
    private long dsiUid;

    /**
     * Foreign key for DownloadSet.dsUid (many to one relationship)
     */
    @UmIndexField
    private long dsiDsUid;

    @UmIndexField
    private long dsiContentEntryUid;

    public DownloadSetItem() {

    }

    public DownloadSetItem(DownloadSet set, ContentEntry contentEntry) {
        this.dsiDsUid = set.getDsUid();
        this.dsiContentEntryUid = contentEntry.getContentEntryUid();
    }

    public DownloadSetItem(long downloadSetUid, long contentEntryUid){
        this.dsiDsUid = downloadSetUid;
        this.dsiContentEntryUid = contentEntryUid;
    }

    public long getDsiUid() {
        return dsiUid;
    }

    public void setDsiUid(long dsiUid) {
        this.dsiUid = dsiUid;
    }

    public long getDsiDsUid() {
        return dsiDsUid;
    }

    public void setDsiDsUid(long dsiDsUid) {
        this.dsiDsUid = dsiDsUid;
    }

    public long getDsiContentEntryUid() {
        return dsiContentEntryUid;
    }

    public void setDsiContentEntryUid(long dsiContentEntryUid) {
        this.dsiContentEntryUid = dsiContentEntryUid;
    }
}
