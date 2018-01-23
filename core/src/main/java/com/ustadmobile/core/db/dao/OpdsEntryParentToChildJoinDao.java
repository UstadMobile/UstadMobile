package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;

/**
 * Created by mike on 1/23/18.
 */

public abstract class OpdsEntryParentToChildJoinDao {

    @UmInsert
    public abstract void insert(OpdsEntryParentToChildJoin entry);
}
