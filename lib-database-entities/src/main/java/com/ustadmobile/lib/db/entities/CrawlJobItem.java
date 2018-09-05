package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 3/3/18.
 */

@UmEntity
public class CrawlJobItem {

    @UmPrimaryKey(autoIncrement = true)
    private Integer id;

    private int crawlJobId;

    private String uri;

    private int status;

    private int depth;

    private String opdsEntryUuid;

    public CrawlJobItem() {

    }

    public CrawlJobItem(int crawlJobId, String uri, int status, int depth) {
        this.crawlJobId = crawlJobId;
        this.uri = uri;
        this.status = status;
        this.depth = depth;
    }

    public CrawlJobItem(int crawlJobId, OpdsEntryWithRelations entry, int status, int depth){
        this.crawlJobId = crawlJobId;
        this.opdsEntryUuid = entry.getUuid();
        this.status =status;
        this.depth = depth;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCrawlJobId() {
        return crawlJobId;
    }

    public void setCrawlJobId(int crawlJobId) {
        this.crawlJobId = crawlJobId;
    }

    public int getDownloadJobId() {
        return crawlJobId;
    }

    public void setDownloadJobId(int downloadJobId) {
        this.crawlJobId = downloadJobId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getOpdsEntryUuid() {
        return opdsEntryUuid;
    }

    public void setOpdsEntryUuid(String opdsEntryUuid) {
        this.opdsEntryUuid = opdsEntryUuid;
    }
}
