package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheAncestorDao;

import java.util.List;

/**
 * Created by mike on 3/24/18.
 */
@Dao
public abstract class OpdsEntryStatusCacheAncestorDaoAndroid extends OpdsEntryStatusCacheAncestorDao{

    @Override
    @Insert
    public abstract void insertAll(List<OpdsEntryStatusCacheAncestor> ancestorList);


    @Query(GET_ANCESTOR_ENTRIES_RECURSIVE_SQL_TO_INSERT)
    public abstract List<OpdsEntryStatusCacheAncestor> findAncestorsToAdd(List<String> entryIds);
}
