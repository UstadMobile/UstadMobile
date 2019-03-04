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

    @UmQuery("UPDATE ContentEntryStatus SET totalSize = \n" +
            "\t(SELECT fileSize FROM ContentEntryFile  \n" +
            "\tJOIN ContentEntryContentEntryFileJoin \n" +
            "\tON ContentEntryFile.contentEntryFileUid =  ContentEntryContentEntryFileJoin.cecefjContentEntryFileUid  \n" +
            "\tAND ContentEntryContentEntryFileJoin.cecefjContentEntryUid = ContentEntryStatus.cesUid  \n" +
            "\tORDER BY ContentEntryFile.lastModified DESC LIMIT 1),\n" +
            "bytesDownloadSoFar = \n" +
            "\t(SELECT downloadedSoFar FROM DownloadJobItem \n" +
            "\tLEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid \n" +
            "\tWHERE DownloadSetItem.dsiContentEntryUid = ContentEntryStatus.cesUid LIMIT 1),\n" +
            "downloadStatus = \n" +
            "\t(SELECT djiStatus FROM DownloadJobItem \n" +
            "\tLEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid \n" +
            "\tWHERE DownloadSetItem.dsiContentEntryUid = ContentEntryStatus.cesUid LIMIT 1),\n" +
            "downloadSpeed = \n" +
            "\t(SELECT downloadSpeed FROM DownloadJobItem \n" +
            "\tLEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid \n" +
            "\tWHERE DownloadSetItem.dsiContentEntryUid = ContentEntryStatus.cesUid LIMIT 1)\n" +
            "WHERE cesUid = :contentEntryUid")
    public abstract void updateLeaf(long contentEntryUid);

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

}
