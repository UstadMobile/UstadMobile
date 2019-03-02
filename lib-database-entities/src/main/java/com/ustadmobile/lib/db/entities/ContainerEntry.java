package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContainerEntry {

    @UmPrimaryKey(autoIncrement = true)
    private long ceUid;

    private String cePath;

    private long ceCefUid;

    public ContainerEntry() {

    }

    public ContainerEntry(String cePath, ContainerEntryFile entryFile) {
        this.cePath = cePath;
        this.ceCefUid = entryFile.getCefUid();
    }


    public long getCeUid() {
        return ceUid;
    }

    public void setCeUid(long ceUid) {
        this.ceUid = ceUid;
    }

    public String getCePath() {
        return cePath;
    }

    public void setCePath(String cePath) {
        this.cePath = cePath;
    }

    public long getCeCefUid() {
        return ceCefUid;
    }

    public void setCeCefUid(long ceCefUid) {
        this.ceCefUid = ceCefUid;
    }
}
