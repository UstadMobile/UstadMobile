package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/26/18.
 */

@UmEntity
public class DownloadJob {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private int status;

    private long timeRequested;

    private long timeCompleted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimeRequested() {
        return timeRequested;
    }

    public void setTimeRequested(long timeRequested) {
        this.timeRequested = timeRequested;
    }

    public long getTimeCompleted() {
        return timeCompleted;
    }

    public void setTimeCompleted(long timeCompleted) {
        this.timeCompleted = timeCompleted;
    }
}
