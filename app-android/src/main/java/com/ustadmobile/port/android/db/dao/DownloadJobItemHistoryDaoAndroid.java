package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.dao.DownloadJobItemHistoryDao;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;

import java.util.List;

/**
 * Created by mike on 2/2/18.
 */

@Dao
public abstract class DownloadJobItemHistoryDaoAndroid extends DownloadJobItemHistoryDao {

    @Override
    @Query("SELECT * FROM DownloadJobItemHistory WHERE networkNode = :nodeId AND startTime >= :since")
    public abstract List<DownloadJobItemHistory> findHistoryItemsByNetworkNodeSince(int nodeId, long since);

    @Override
    @Query("SELECT * From DownloadJobItemHistory WHERE downloadJobItemId = :downloadJobItemId")
    public abstract List<DownloadJobItemHistory> findHistoryItemsByDownloadJobItem(int downloadJobItemId);

    @Insert
    public abstract long insert(DownloadJobItemHistory downloadJobItemHistory);

    @Update
    public abstract void update(DownloadJobItemHistory downloadJobItemHistory);
}
