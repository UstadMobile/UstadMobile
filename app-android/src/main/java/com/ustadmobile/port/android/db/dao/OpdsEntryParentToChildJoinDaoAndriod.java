package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mike on 1/23/18.
 */
@Dao
public abstract class OpdsEntryParentToChildJoinDaoAndriod extends OpdsEntryParentToChildJoinDao implements UmDaoAndroid{

    private ExecutorService executorService;

    @Override
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(OpdsEntryParentToChildJoin entry);

    @Override
    public void insertAsync(OpdsEntryParentToChildJoin entry, UmCallback<Integer> callback) {
        executorService.execute(() -> callback.onSuccess((int)insert(entry)));
    }

    @Override
    @Query("Select * From OpdsEntryParentToChildJoin WHERE parentEntry = :parentId and childEntry = :childId")
    public abstract List<OpdsEntryParentToChildJoin> findByParentAndEntry(String parentId, String childId);

    @Override
    @Query("Select * From OpdsEntryParentToChildJoin")
    public abstract List<OpdsEntryParentToChildJoin> findAll();

    @Query("SELECT MAX(childIndex) FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId")
    public abstract int getNumEntriesByParent(String parentId);

    @Query("DELETE FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId AND childEntry IN (:childId)")
    public abstract int deleteByParentIdAndChildId(String parentId, List<String> childId);

    public void deleteByParentIdAndChildIdAsync(String parentId, List<String> childId, UmCallback<Integer> callback) {
        executorService.execute(() -> callback.onSuccess(deleteByParentIdAndChildId(parentId, childId)));
    }


}
