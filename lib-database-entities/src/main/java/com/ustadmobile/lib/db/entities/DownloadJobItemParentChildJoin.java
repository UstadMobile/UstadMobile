package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

@UmEntity
public class DownloadJobItemParentChildJoin {

    @UmPrimaryKey
    private long djiPcjUid;

    private long djiParentDjiUid;

    private long djiChildDjiUid;

    private long djiCepcjUid;

    public DownloadJobItemParentChildJoin() {

    }

    public DownloadJobItemParentChildJoin(long djiParentDjiUid, long djiChildDjiUid, long djiCepcjUid) {
        this.djiParentDjiUid = djiParentDjiUid;
        this.djiChildDjiUid = djiChildDjiUid;
        this.djiCepcjUid = djiCepcjUid;
    }

    public long getDjiPcjUid() {
        return djiPcjUid;
    }

    public void setDjiPcjUid(long djiPcjUid) {
        this.djiPcjUid = djiPcjUid;
    }

    public long getDjiParentDjiUid() {
        return djiParentDjiUid;
    }

    public void setDjiParentDjiUid(long djiParentDjiUid) {
        this.djiParentDjiUid = djiParentDjiUid;
    }

    public long getDjiChildDjiUid() {
        return djiChildDjiUid;
    }

    public void setDjiChildDjiUid(long djiChildDjiUid) {
        this.djiChildDjiUid = djiChildDjiUid;
    }

    public long getDjiCepcjUid() {
        return djiCepcjUid;
    }

    public void setDjiCepcjUid(long djiCepcjUid) {
        this.djiCepcjUid = djiCepcjUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadJobItemParentChildJoin that = (DownloadJobItemParentChildJoin) o;

        if (djiPcjUid != that.djiPcjUid) return false;
        if (djiParentDjiUid != that.djiParentDjiUid) return false;
        return djiChildDjiUid == that.djiChildDjiUid;
    }

    @Override
    public int hashCode() {
        int result = (int) (djiPcjUid ^ (djiPcjUid >>> 32));
        result = 31 * result + (int) (djiParentDjiUid ^ (djiParentDjiUid >>> 32));
        result = 31 * result + (int) (djiChildDjiUid ^ (djiChildDjiUid >>> 32));
        return result;
    }
}
