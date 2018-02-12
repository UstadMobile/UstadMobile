package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJobItem;

import java.util.List;

/**
 * Created by mike on 2/5/18.
 */

public abstract class DownloadJobItemDao {

    @UmInsert
    public abstract void insertList(List<DownloadJobItem> jobItems);

    @UmQuery("Update DownloadJobItem SET " +
            "status = :status, downloadedSoFar = :downlaodedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed" +
            " WHERE id = :downloadJobItemId")
    public abstract void updateDownloadJobItemStatus(int downloadJobItemId, int status,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed);

    public void updateDownloadJobItemStatus(DownloadJobItem item) {
        updateDownloadJobItemStatus(item.getId(), item.getStatus(), item.getDownloadedSoFar(),
                item.getDownloadLength(), item.getCurrentSpeed());
    }

    @UmQuery("Select * FROM DownloadJobItem WHERE entryId = :entryId AND status BETWEEN :statusFrom AND :statusTo")
    public abstract UmLiveData<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRange(String entryId,
                                                                                  int statusFrom,
                                                                                  int statusTo);


}
