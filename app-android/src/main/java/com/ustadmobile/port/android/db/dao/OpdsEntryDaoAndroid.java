package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryDaoAndroid extends OpdsEntryDao{

    @Insert
    @Override
    public abstract void insert(OpdsEntry entry);
}
