package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.DownloadSetDao;
import com.ustadmobile.lib.db.entities.DownloadSet;

/**
 * Created by mike on 2/2/18.
 */
@Dao
public abstract class DownloadSetDaoAndroid extends DownloadSetDao {

    @Override
    @Insert
    public abstract long insert(DownloadSet job);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertOrReplace(DownloadSet set);

    @Override
    @Query("SELECT * FROM DownloadSet WHERE rootOpdsUuid = :rootEntryUuid")
    public abstract DownloadSet findByRootEntry(String rootEntryUuid);

    @Override
    public UmLiveData<DownloadSet> getByIdLive(int id) {
        return new UmLiveDataAndroid<>(getByIdLiveR(id));
    }

    @Override
    @Update
    public abstract void update(DownloadSet job);

    @Query("SELECT * From DownloadSet WHERE id = :id")
    public abstract LiveData<DownloadSet> getByIdLiveR(int id);

    @Query("SELECT * FROM DownloadSet WHERE id = :id")
    public abstract DownloadSet findById(int id);


}
