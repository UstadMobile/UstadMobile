package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntry;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */
@UmDao
public abstract class OpdsEntryDao {

    @UmInsert(onConflict =  UmOnConflictStrategy.REPLACE)
    public abstract long insert(OpdsEntry entry);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insertList(List<OpdsEntry> entries);


    @UmQuery("SELECT (COUNT(*) > 0) From OpdsEntry WHERE uuid = :entryId")
    public abstract UmLiveData<Boolean> isEntryPresent(String entryId);

    @UmQuery("Select title From OpdsEntry Where uuid = :uuid")
    public abstract String findTitleByUuid(String uuid);

    @UmQuery("Select title From OpdsEntry Where uuid = :uuid")
    public abstract void findTitleByUuidAsync(String uuid, UmCallback<String> callback);


}
