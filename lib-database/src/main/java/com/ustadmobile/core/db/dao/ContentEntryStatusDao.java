package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContentEntryStatus;
import com.ustadmobile.lib.db.sync.dao.BaseDao;

import java.util.List;

@UmDao
public abstract class ContentEntryStatusDao implements BaseDao<ContentEntryStatus> {

    public void refresh() {
        System.out.println("Update content entry status");
    }

    @UmQuery("DELETE FROM ContentEntryStatus")
    public abstract void deleteAll(UmCallback<Void> callback);

    @UmQuery("UPDATE ContentEntryStatus SET bytesDownloadSoFar = :bytesDownloadSoFar " +
            "WHERE cesUid = :contentEntryUid")
    public abstract void updateLeafBytesDownloaded(long contentEntryUid, long bytesDownloadSoFar);


    @UmQuery("UPDATE ContentEntryStatus SET downloadStatus = :downloadStatus WHERE cesUid = :contentEntryUid")
    public abstract void updateDownloadStatus(long contentEntryUid, int downloadStatus);

//    @UmQuery("UPDATE ContentEntryStatus SET " +
//            "totalSize = 42 " +
//            "WHERE cesUid IN :updateList")
//    public abstract void updateItems(List<Long> updateList);

    @UmQuery("SELECT * FROM ContentEntryStatus WHERE invalidated")
    public abstract List<ContentEntryStatus> findAllInvalidated();

    @UmQuery("Select * FROM ContentEntryStatus where cesUid = :parentUid")
    public abstract UmLiveData<ContentEntryStatus> findContentEntryStatusByUid(long parentUid);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insertOrAbort(List<ContentEntryStatus> statusList);

    @UmQuery("DELETE FROM ContentEntryStatus WHERE cesUid = :cesUid")
    public abstract void deleteByFileUids(long cesUid);

}
