package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 1/16/18.
 */
@UmDao
public abstract class OpdsLinkDao {

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insert(List<OpdsLink> links);

    @UmQuery("SELECT * From OpdsLink WHERE entryUuid = :entryUuid")
    public abstract List<OpdsLink> findLinkByEntryId(String entryUuid);

    @UmQuery("SELECT * FROM OpdsLink")
    public abstract List<OpdsLink> getAllLinks();

}
