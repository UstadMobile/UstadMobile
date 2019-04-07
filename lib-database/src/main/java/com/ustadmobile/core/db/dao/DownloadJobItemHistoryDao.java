package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;

import java.util.List;

/**
 * Created by mike on 2/2/18.
 */

@UmDao
public abstract class DownloadJobItemHistoryDao {

    @UmQuery("SELECT * FROM DownloadJobItemHistory WHERE networkNode = :nodeId AND startTime >= :since")
    public abstract List<DownloadJobItemHistory> findHistoryItemsByNetworkNodeSince(long nodeId, long since);

    @UmInsert
    public abstract long insert(DownloadJobItemHistory downloadJobItemHistory);

    @UmUpdate
    public abstract void update(DownloadJobItemHistory downloadJobItemHistory);

    @UmQuery("DELETE FROM DownloadJobItemHistory")
    public abstract void deleteAll(UmCallback<Void> callback);

    @UmInsert
    public abstract void insertList(List<DownloadJobItemHistory> historyList);


    @UmQuery("SELECT * From DownloadJobItemHistory WHERE downloadJobItemId = :downloadJobItemId")
    public abstract List<DownloadJobItemHistory> findHistoryItemsByDownloadJobItem(long downloadJobItemId);
}
