package com.ustadmobile.lib.db.entities;

public class ContentEntryFileWithContentEntryFileStatusAndContentEntryId extends ContentEntryFile {

    private String filePath;

    private int cefsUid;

    private String entryId;

    private long contentEntryUid;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String sourceUrl) {
        this.entryId = sourceUrl;
    }

    public long getContentEntryUid() {
        return contentEntryUid;
    }

    public void setContentEntryUid(long contentEntryUid) {
        this.contentEntryUid = contentEntryUid;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getCefsUid() {
        return cefsUid;
    }

    public void setCefsUid(int cefsUid) {
        this.cefsUid = cefsUid;
    }
}
