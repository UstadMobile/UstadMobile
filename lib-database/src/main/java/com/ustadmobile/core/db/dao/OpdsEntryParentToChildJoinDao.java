package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

import java.util.List;

/**
 * Created by mike on 1/23/18.
 */
@UmDao
public abstract class OpdsEntryParentToChildJoinDao {

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insert(OpdsEntryParentToChildJoin entry);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insertAll(List<OpdsEntryParentToChildJoin> entryList);

    @UmInsert
    public abstract void insertAsync(OpdsEntryParentToChildJoin entry, UmCallback<Long> callback);

    @UmInsert
    public abstract void insertAsLastEntryForParentAsync(OpdsEntryParentToChildJoin entry, UmCallback<Long> callback);

    public long insertAsLastEntryForParent(OpdsEntryParentToChildJoin entry) {
        entry.setChildIndex(getNumEntriesByParent(entry.getParentEntry()) + 1);
        return insert(entry);
    }

    @UmQuery("Select * From OpdsEntryParentToChildJoin WHERE parentEntry = :parentId and childEntry = :childId")
    public abstract List<OpdsEntryParentToChildJoin> findByParentAndEntry(String parentId, String childId);

    @UmQuery("Select * From OpdsEntryParentToChildJoin")
    public abstract List<OpdsEntryParentToChildJoin> findAll();

    @UmQuery("SELECT MAX(childIndex) FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId")
    public abstract int getNumEntriesByParent(String parentId);

    @UmQuery("DELETE FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId AND childEntry IN (:childId)")
    public abstract int deleteByParentIdAndChildId(String parentId, List<String> childId);

    @UmQuery("DELETE FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId AND childEntry IN (:childId)")
    public abstract void deleteByParentIdAndChildIdAsync(String parentId, List<String> childId,
                                                        UmCallback<Integer> callback);



}
