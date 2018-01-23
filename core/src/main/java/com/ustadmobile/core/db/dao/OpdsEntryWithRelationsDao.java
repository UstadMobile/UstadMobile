package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

/**
 * Created by mike on 1/15/18.
 */

public abstract class OpdsEntryWithRelationsDao {

    @UmQuery("SELECT * from OpdsEntry WHERE url = :url")
    public abstract UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url);

    @UmQuery("SELECT * from OpdsEntry WHERE feed_id = :feedId")
    public abstract UmProvider<OpdsEntryWithRelations> findEntriesByFeed(String feedId);

    @UmQuery("SELECT * from OpdsEntry INNER JOIN OpdsEntryToParentOpdsEntry on OpdsEntry.id = OpdsEntry.id WHERE OpdsEntryToParentOpdsEntry.parentEntry = :parentId")
    public abstract UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId);

    @UmQuery("SELECT * from OpdsEntry where id = :uuid")
    public abstract UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid);


}
