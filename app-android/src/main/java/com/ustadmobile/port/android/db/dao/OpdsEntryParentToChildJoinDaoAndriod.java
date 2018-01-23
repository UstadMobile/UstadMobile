package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

/**
 * Created by mike on 1/23/18.
 */
@Dao
public abstract class OpdsEntryParentToChildJoinDaoAndriod extends OpdsEntryParentToChildJoinDao {

    @Override
    @Insert
    public abstract void insert(OpdsEntryParentToChildJoin entry);
}
