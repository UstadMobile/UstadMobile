package com.ustadmobile.port.android.db.dao;

import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryWithRelationsDaoAndroid extends OpdsEntryWithRelationsDao {

    @Override
    public UmLiveData<List<OpdsEntryWithRelations>> getEntryByUrl(String url) {
        return null;
    }

    @Override
    public UmProvider<OpdsEntryWithRelations> findEntriesByFeed(int feedId) {
        return () -> findEntriesByFeedR(feedId);
    }

    @Query("Select * From OpdsEntry Where feedId = :feedId")
    public abstract DataSource.Factory<Integer, OpdsEntryWithRelations> findEntriesByFeedR(int feedId);
}
