package com.ustadmobile.core.db.dao;

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


    @UmQuery("SELECT * From DownloadSetItem WHERE dsiUid = :dsiUid")
    public abstract DownloadSetItem findById(int dsiUid);

    /**
     * Insert a list of DownloadSetItem entities
     * @param jobItems List of DownloadSetItem entities to insert
     */
    @UmInsert
    public abstract void insert(List<DownloadSetItem> jobItems);

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

    @UmQuery("DELETE FROM DownloadSetItem WHERE dsiDsUid = :dsiDsUid")
    public abstract int deleteByDownloadSetUid(long dsiDsUid);



}
