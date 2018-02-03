package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;

import java.util.List;

/**
 * Created by mike on 2/2/18.
 */

public abstract class DownloadJobItemHistoryDao {

    public abstract List<DownloadJobItemHistory> findHistoryItemsByNetworkNodeSince(int nodeId, long since);

}
