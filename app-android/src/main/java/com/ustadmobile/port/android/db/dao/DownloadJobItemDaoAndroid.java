package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import com.ustadmobile.core.db.dao.DownloadJobItemDao;
import com.ustadmobile.lib.db.entities.DownloadJobItem;

import java.util.List;

/**
 * Created by mike on 2/5/18.
 */
@Dao
public abstract class DownloadJobItemDaoAndroid extends DownloadJobItemDao {

    @Override
    @Insert
    public abstract void insertList(List<DownloadJobItem> jobItems);
}
