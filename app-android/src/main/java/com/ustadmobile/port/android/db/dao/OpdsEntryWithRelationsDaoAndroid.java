package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@Dao
public abstract class OpdsEntryWithRelationsDaoAndroid extends OpdsEntryWithRelationsDao {

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryId,
                                                            OpdsEntry.OpdsItemLoadCallback callback) {
        return new UmLiveDataAndroid<>(getEntryByUrlR(url));
    }

    @Query("SELECT * From OpdsEntry Where url = :url")
    public abstract LiveData<OpdsEntryWithRelations> getEntryByUrlR(String url);

    @Query("SELECT OpdsEntry.* from OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.id = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId")
    public abstract DataSource.Factory<Integer, OpdsEntryWithRelations> findEntriesByParentR(String parentId);

    @Override
    public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId){
        return new UmLiveDataAndroid<>(findEntriesByParentAsListR(parentId));
    }

    @Query("SELECT OpdsEntry.* from OpdsEntry INNER JOIN OpdsEntryParentToChildJoin on OpdsEntry.id = OpdsEntryParentToChildJoin.childEntry WHERE OpdsEntryParentToChildJoin.parentEntry = :parentId")
    public abstract LiveData<List<OpdsEntryWithRelations>> findEntriesByParentAsListR(String parentId);

    @Override
    public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
        return () -> findEntriesByParentR(parentId);
    }

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid) {
        return new UmLiveDataAndroid<OpdsEntryWithRelations>(getEntryByUuidR(uuid));
    }

    @Query("SELECT * from OpdsEntry where id = :uuid")
    public abstract LiveData<OpdsEntryWithRelations> getEntryByUuidR(String uuid);

    @Override
    @Query("SELECT id FROM OpdsEntry WHERE url = :url")
    public abstract String getUuidForEntryUrl(String url);
}
