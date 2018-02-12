package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.lib.db.entities.DownloadJobItem;

import java.util.List;

/**
 * Created by mike on 2/5/18.
 */
@Dao
public abstract class DownloadJobItemDaoAndroid extends DownloadJobItemDao {

    @Override
    @Insert
    public abstract void insertList(List<DownloadJobItem> jobItems);

    @Override
    @Query("Update DownloadJobItem SET " +
            "status = :status, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE id = :downloadJobItemId")
    public abstract void updateDownloadJobItemStatus(int downloadJobItemId, int status, long downloadedSoFar, long downloadLength, long currentSpeed);

    @Override
    public UmLiveData<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRange(String entryId, int statusFrom, int statusTo) {
        return new UmLiveDataAndroid<>(findDownloadJobItemByEntryIdAndStatusRangeR(entryId, statusFrom, statusTo));
    }

    @Query("SELECT * FROM DownloadJobItem WHERE entryId = :entryId AND status BETWEEN :statusFrom AND :statusTo")
    public abstract LiveData<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRangeR(String entryId, int statusFrom, int statusTo);
}
