package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;

import java.util.List;

/**
 * Created by mike on 2/2/18.
 */

public abstract class DownloadJobItemHistoryDao {

    public abstract List<DownloadJobItemHistory> findHistoryItemsByNetworkNodeSince(int nodeId, long since);

    @UmInsert
    public abstract long insert(DownloadJobItemHistory downloadJobItemHistory);

    public abstract void update(DownloadJobItemHistory downloadJobItemHistory);

    @UmQuery("SELECT * From DownloadJobItemHistory WHERE downloadJobItemId = :downloadJobItemId")
    public abstract List<DownloadJobItemHistory> findHistoryItemsByDownloadJobItem(int downloadJobItemId);
}
