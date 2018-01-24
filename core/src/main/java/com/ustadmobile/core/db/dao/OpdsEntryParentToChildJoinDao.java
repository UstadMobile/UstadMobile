package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

import java.util.List;

/**
 * Created by mike on 1/23/18.
 */

public abstract class OpdsEntryParentToChildJoinDao {

    @UmInsert
    public abstract void insert(OpdsEntryParentToChildJoin entry);

    public abstract List<OpdsEntryParentToChildJoin> findByParentAndEntry(String parentId, String childId);

    public abstract List<OpdsEntryParentToChildJoin> findAll();

    @UmQuery("SELECT MAX(childIndex) FROM OpdsEntryParentToChildJoin WHERE parentEntry = :parentId")
    public abstract int getNumEntriesByParent(String parentId);

}
