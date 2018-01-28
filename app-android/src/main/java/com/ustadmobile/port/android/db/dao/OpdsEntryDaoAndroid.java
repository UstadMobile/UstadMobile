package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryDaoAndroid extends OpdsEntryDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Override
    public abstract long insert(OpdsEntry entry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertList(List<OpdsEntry> entries);

    @Query("SELECT (COUNT(*) > 0) From OpdsEntry WHERE uuid = :entryId")
    public abstract LiveData<Boolean> isEntryPresentR(String entryId);

    @Override
    public UmLiveData<Boolean> isEntryPresent(String entryId) {
        return new UmLiveDataAndroid<>(isEntryPresentR(entryId));
    }
}
