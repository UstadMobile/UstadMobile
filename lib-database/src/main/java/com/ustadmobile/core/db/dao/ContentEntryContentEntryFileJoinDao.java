package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;

import java.util.List;

import com.ustadmobile.lib.db.entities.ContentEntryWithFileJoinStatus;
import com.ustadmobile.lib.db.sync.dao.SyncableDao;

/**
 * Deprecated: this is being replaced with Container which support de-duplicating entries
 */
@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@UmRepository
@Deprecated
public abstract class ContentEntryContentEntryFileJoinDao
        implements SyncableDao<ContentEntryContentEntryFileJoin, ContentEntryContentEntryFileJoinDao> {

    @UmQuery("SELECT * FROM ContentEntryContentEntryFileJoin WHERE " +
            "cecefjContentEntryUid = :parentEntryContentUid")
    public abstract List<ContentEntryContentEntryFileJoin> findChildByParentUUid(long parentEntryContentUid);

    @UmUpdate
    public abstract void update(ContentEntryContentEntryFileJoin entity);

    @UmQuery("SELECT ContentEntryContentEntryFileJoin.* FROM ContentEntryContentEntryFileJoin " +
            "LEFT JOIN ContentEntry ON ContentEntryContentEntryFileJoin.cecefjContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.publik")
    public abstract List<ContentEntryContentEntryFileJoin> getPublicContentEntryContentEntryFileJoins();

    @UmQuery("DELETE FROM ContentEntryContentEntryFileJoin WHERE cecefjContentEntryFileUid = :fileUid " +
            "and cecefjContentEntryUid = :contentUid")
    public abstract void deleteByUid(long fileUid, long contentUid);


    @UmQuery("SELECT ContentEntryFile.*, ContentEntryFileStatus.*, ContentEntryContentEntryFileJoin.* FROM ContentEntryContentEntryFileJoin " +
            "LEFT JOIN ContentEntryFile ON ContentEntryFile.contentEntryFileUid = ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid " +
            "LEFT JOIN ContentEntryFileStatus ON ContentEntryFileStatus.cefsContentEntryFileUid = ContentEntryFile.contentEntryFileUid " +
            "WHERE ContentEntryContentEntryFileJoin.cecefjContainerUid IS NULL OR ContentEntryContentEntryFileJoin.cecefjContainerUid = 0")
    public abstract List<ContentEntryWithFileJoinStatus> findAllFiles();


    @UmQuery("UPDATE ContentEntryContentEntryFileJoin SET cecefjContainerUid = :containerUid WHERE cecefjUid = :joinUid")
    public abstract void updateContainerId(long containerUid, long joinUid);
}
