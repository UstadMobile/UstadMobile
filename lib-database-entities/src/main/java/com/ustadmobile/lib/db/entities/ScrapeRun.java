package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class ScrapeRun {

    @UmPrimaryKey(autoIncrement = true)
    private int scrapeRunUid;

    private String scrapeType;

    private int status;

    public ScrapeRun() {

    }

    public ScrapeRun(String scrapeType, int status) {
        this.scrapeType = scrapeType;
        this.status = status;
    }

    public int getScrapeRunUid() {
        return scrapeRunUid;
    }

    public void setScrapeRunUid(int scrapeRunUid) {
        this.scrapeRunUid = scrapeRunUid;
    }

    public String getScrapeType() {
        return scrapeType;
    }

    public void setScrapeType(String scrapeType) {
        this.scrapeType = scrapeType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
