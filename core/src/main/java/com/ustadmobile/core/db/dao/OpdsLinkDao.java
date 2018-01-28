package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.List;
import java.util.UUID;

/**
 * Created by mike on 1/16/18.
 */

public abstract class OpdsLinkDao {

    @UmInsert
    public abstract void insert(List<OpdsLink> links);

    @UmQuery("SELECT * From OpdsLink WHERE entryId = :entryUuid")
    public abstract List<OpdsLink> findLinkByEntryId(String entryUuid);

    public abstract List<OpdsLink> getAllLinks();

}
