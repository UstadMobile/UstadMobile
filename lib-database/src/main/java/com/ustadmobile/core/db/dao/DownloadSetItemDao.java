package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadSetItem;

import java.util.List;

/**
 * DAO for the DownloadSetItem entity
 */
@UmDao
public abstract class DownloadSetItemDao {

    /**
     * Insert a list of DownloadSetItem entities
     * @param jobItems List of DownloadSetItem entities to insert
     */
    @UmInsert
    public abstract void insert(List<DownloadSetItem> jobItems);

    @UmQuery("DELETE FROM DownloadSetItem")
    public abstract void deleteAll(UmCallback<Void> callback);

    /**
     * Insert a single DownloadSetItem
     *
     * @param item DownloadSetItem to insert
     *
     * @return the primary key of the inserted entity
     */
    @UmInsert
    public abstract long insert(DownloadSetItem item);

    @UmQuery("SELECT dsiDsUid FROM DownloadSetItem WHERE dsiContentEntryUid = :contentEntryUid")
    public abstract long findDownloadSetUidByContentEntryUid(long contentEntryUid);

    @UmQuery("SELECT dsiUid FROM DownloadSetItem WHERE dsiDsUid = :dsiDsUid")
    public abstract List<Long> findBySetUid(long dsiDsUid);

}
