package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

import java.util.List;

/**
 * Created by mike on 1/23/18.
 */

public abstract class OpdsEntryParentToChildJoinDao {

    @UmInsert
    public abstract long insert(OpdsEntryParentToChildJoin entry);

    public abstract void insertAsync(OpdsEntryParentToChildJoin entry, UmCallback<Integer> callback);


    public abstract List<OpdsEntryParentToChildJoin> findByParentAndEntry(String parentId, String childId);

    public abstract List<OpdsEntryParentToChildJoin> findAll();

    @UmQuery("SELECT MAX(childIndex) FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId")
    public abstract int getNumEntriesByParent(String parentId);

    @UmQuery("DELETE FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId AND childEntry IN (:childId)")
    public abstract int deleteByParentIdAndChildId(String parentId, List<String> childId);

    @UmQuery("DELETE FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId AND childEntry IN (:childId)")
    public abstract void deleteByParentIdAndChildIdAsync(String parentId, List<String> childId,
                                                        UmCallback<Integer> callback);



}
