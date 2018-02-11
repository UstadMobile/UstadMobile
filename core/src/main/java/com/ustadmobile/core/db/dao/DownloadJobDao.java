package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobWithRelations;

/**
 * Created by mike on 1/31/18.
 */

public abstract class DownloadJobDao {

    @UmInsert
    public abstract long insert(DownloadJob job);

    @UmQuery("Update DownloadJob SET status = :status, timeRequested = :timeRequested WHERE id = :id")
    public abstract long queueDownload(int id, int status, long timeRequested);


    @UmQuery("SELECT * FROM DownloadJob WHERE status > 0 AND status <= 10 ORDER BY timeRequested LIMIT 1")
    protected abstract DownloadJobWithRelations findNextDownloadJob();

    @UmQuery("UPDATE DownloadJob SET status = :status WHERE id = :jobId")
    public abstract long updateJobStatus(int id, int status);

    @UmQuery("UPDATE DownloadJob SET status = :setTo WHERE status BETWEEN :rangeFrom AND :rangeTo")
    public abstract void updateJobStatusByRange(int rangeFrom, int rangeTo, int setTo);

    public abstract void update(DownloadJob job);

    /**
     * Convenience method as a transaction to avoid the possibility of getting the same
     * download job running twice.
     *
     * @return
     */
    public DownloadJobWithRelations findNextDownloadJobAndSetStartingStatus(){
        DownloadJobWithRelations nextJob = findNextDownloadJob();
        if(nextJob != null){
            updateJobStatus(nextJob.getId(), NetworkTask.STATUS_STARTING);
        }

        return nextJob;
    }

    @UmQuery("SELECT * From DownloadJob where id = :id")
    public abstract UmLiveData<DownloadJobWithRelations> getByIdLive(int id);


}
