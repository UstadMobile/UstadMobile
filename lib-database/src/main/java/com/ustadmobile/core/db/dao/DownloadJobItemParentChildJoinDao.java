package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;

import java.util.List;

@UmDao
public abstract class DownloadJobItemParentChildJoinDao {

    @UmQuery("SELECT DownloadJobItemParentChildJoin.* FROM DownloadJobItemParentChildJoin " +
            "WHERE djiChildDjiUid = :childDjiUid ")
    public abstract List<DownloadJobItemParentChildJoin> findParentsByChildUid(long childDjiUid);

    @UmQuery("SELECT DownloadJobItemParentChildJoin.* FROM DownloadJobItemParentChildJoin " +
            " LEFT JOIN DownloadJobItem ON DownloadJobItemParentChildJoin.djiParentDjiUid = DownloadJobItem.djiUid " +
            " WHERE DownloadJobItem.djiDjUid = :djUid")
    public abstract List<DownloadJobItemParentChildJoin> findParentChildJoinsByDownloadJobUids(int djUid);

    @UmInsert
    public abstract void insertList(List<DownloadJobItemParentChildJoin> joins);

}
