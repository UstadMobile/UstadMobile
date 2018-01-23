package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryDaoAndroid extends OpdsEntryDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Override
    public abstract long insert(OpdsEntry entry);
}
