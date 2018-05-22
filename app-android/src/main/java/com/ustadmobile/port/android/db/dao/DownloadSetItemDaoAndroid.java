package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadSetItemDao;
import com.ustadmobile.lib.db.entities.DownloadSetItem;

import java.util.List;

/**
 * Created by mike on 2/5/18.
 */
@Dao
public abstract class DownloadSetItemDaoAndroid extends DownloadSetItemDao {

    @Override
    @Insert
    public abstract void insertList(List<DownloadSetItem> jobItems);

    @Override
    @Query("SELECT * FROM DownloadSetItem WHERE entryId = :entryId AND downloadSetId = :downloadSetId")
    public abstract DownloadSetItem findByEntryId(String entryId, int downloadSetId);

    @Insert
    @Override
    public abstract long insert(DownloadSetItem item);

}
