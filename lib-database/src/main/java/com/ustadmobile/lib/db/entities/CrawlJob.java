package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 3/6/18.
 */
@UmEntity
public class CrawlJob {

    @UmPrimaryKey(autoIncrement = true)
    private Integer crawlJobId;

    private String rootOpdsUuid;

    private int status;

    private boolean addContainersToDownloadJob = false;

    private int downloadJobId;

    public Integer getCrawlJobId() {
        return crawlJobId;
    }

    public void setCrawlJobId(Integer crawlJobId) {
        this.crawlJobId = crawlJobId;
    }

    public String getRootOpdsUuid() {
        return rootOpdsUuid;
    }

    public void setRootOpdsUuid(String rootOpdsUuid) {
        this.rootOpdsUuid = rootOpdsUuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isAddContainersToDownloadJob() {
        return addContainersToDownloadJob;
    }

    public void setAddContainersToDownloadJob(boolean addContainersToDownloadJob) {
        this.addContainersToDownloadJob = addContainersToDownloadJob;
    }

    public int getDownloadJobId() {
        return downloadJobId;
    }

    public void setDownloadJobId(int downloadJobId) {
        this.downloadJobId = downloadJobId;
    }
}
