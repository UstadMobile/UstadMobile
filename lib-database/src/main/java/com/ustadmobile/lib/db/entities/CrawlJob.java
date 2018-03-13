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

    private int status;

    private int containersDownloadJobId = -1;

    public Integer getCrawlJobId() {
        return crawlJobId;
    }

    public void setCrawlJobId(Integer crawlJobId) {
        this.crawlJobId = crawlJobId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * If >= 0, containers that are found on this crawl will be added as DownloadJobItems to the
     * given download job
     *
     * @return The id of the download job that containers, discovered on this crawl, should be added to
     */
    public int getContainersDownloadJobId() {
        return containersDownloadJobId;
    }

    /**
     * If >= 0, containers that are found on this crawl will be added as DownloadJobItems to the
     * given download job
     *
     * @param containersDownloadJobId
     */
    public void setContainersDownloadJobId(int containersDownloadJobId) {
        this.containersDownloadJobId = containersDownloadJobId;
    }
}
