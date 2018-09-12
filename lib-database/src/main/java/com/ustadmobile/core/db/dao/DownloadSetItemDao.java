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


    @UmQuery("SELECT * From DownloadSetItem WHERE id = :id")
    public abstract DownloadSetItem findById(int id);

    /**
     * Insert a list of DownloadSetItem entities
     * @param jobItems List of DownloadSetItem entities to insert
     */
    @UmInsert
    public abstract void insertList(List<DownloadSetItem> jobItems);

    /**
     * Insert a single DownloadSetItem
     *
     * @param item DownloadSetItem to insert
     *
     * @return the primary key of the inserted entity
     */
    @UmInsert
    public abstract long insert(DownloadSetItem item);

    /**
     * Find the DownloadSetItem for the given OPDS entryId
     *
     * @param entryId OPDS entryId to search by
     * @param downloadSetId Primary Key of the DownloadSet to search in
     * @return DownloadSetItem matching the given arguments, otherwise null
     */
    @UmQuery("SELECT * FROM DownloadSetItem WHERE entryId = :entryId AND downloadSetId = :downloadSetId")
    public abstract DownloadSetItem findByEntryId(String entryId, int downloadSetId);




}
