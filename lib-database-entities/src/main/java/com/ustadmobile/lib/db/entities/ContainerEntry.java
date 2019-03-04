package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ContainerEntry {

    @UmPrimaryKey(autoIncrement = true)
    private long ceUid;

    @UmIndexField
    private long ceContainerUid;

    private String cePath;

    private long ceCefUid;

    public ContainerEntry() {

    }

    public ContainerEntry(String cePath, Container container, ContainerEntryFile entryFile) {
        this.cePath = cePath;
        this.ceCefUid = entryFile.getCefUid();
        this.ceContainerUid = container.getContainerUid();
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

    public long getCeContainerUid() {
        return ceContainerUid;
    }

    public void setCeContainerUid(long ceContainerUid) {
        this.ceContainerUid = ceContainerUid;
    }
}
