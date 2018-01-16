package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import java.util.List;

/**
 * Created by mike on 1/15/18.
 */

public abstract class OpdsEntryWithRelationsDao {

    @UmQuery("SELECT * from OpdsEntry WHERE url = :url")
    public abstract UmLiveData<List<OpdsEntryWithRelations>> getEntryByUrl(String url);

    @UmQuery("SELECT * from OpdsEntry WHERE feed_id = :feedId")
    public abstract UmProvider<OpdsEntryWithRelations> findEntriesByFeed(int feedId);


}
