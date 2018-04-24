package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.List;

@Dao
public abstract class DownloadJobItemDaoAndroid extends DownloadJobItemDao {

    @Override
    @Insert
    public abstract void insertList(List<DownloadJobItem> jobRunItems);

    @Override
    @Insert
    public abstract void insert(DownloadJobItem jobRunItem);

    @Override
    @Query("Update DownloadJobItem SET " +
            "status = :status, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE downloadJobItemId = :downloadJobItemId")
    public abstract void updateDownloadJobItemStatus(int downloadJobItemId, int status, long downloadedSoFar, long downloadLength, long currentSpeed);

    @Override
    @Query("SELECT * FROM DownloadJobItem WHERE downloadJobId = :downloadJobId")
    public abstract List<DownloadJobItem> findAllByDownloadJobId(int downloadJobId);

    @Override
    public UmLiveData<DownloadJobItemWithDownloadSetItem> findDownloadJobItemByEntryIdAndStatusRangeLive(String entryId, int statusFrom, int statusTo) {
        return new UmLiveDataAndroid<>(findDownloadJobItemByEntryIdAndStatusRangeLive_Room(entryId, statusFrom, statusTo));
    }

    //TODO: this seems like it should be a list
    @Query("SELECT * FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE DownloadSetItem.entryId = :entryId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo ")
    public abstract LiveData<DownloadJobItemWithDownloadSetItem> findDownloadJobItemByEntryIdAndStatusRangeLive_Room(String entryId, int statusFrom, int statusTo);


    @Override
    @Query("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE DownloadSetItem.entryId = :entryId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo ")
    public abstract List<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRange(String entryId, int statusFrom, int statusTo);

    @Override
    @Query("SELECT * FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE downloadJobId = :downloadJobId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo ")
    public abstract DownloadJobItemWithDownloadSetItem findByDownloadJobAndStatusRange(int downloadJobId, int statusFrom, int statusTo);


    @Override
    @Query("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            "WHERE DownloadSetItem.entryId = :entryId " +
            "ORDER BY DownloadJobItem.timeFinished DESC")
    public abstract DownloadJobItem findLastFinishedJobItem(String entryId);

    @Override
    @Query("SELECT downloadJobItemId FROM DownloadJobItem WHERE downloadJobId = :downloadJobId")
    public abstract int[] findAllIdsByDownloadJob(int downloadJobId);
}
