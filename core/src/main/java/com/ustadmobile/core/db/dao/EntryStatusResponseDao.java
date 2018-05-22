package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;

import java.util.List;

/**
 * Created by mike on 1/31/18.
 */

public abstract class EntryStatusResponseDao {

    @UmInsert
    public abstract void insert(List<EntryStatusResponse> responses);


    @UmQuery("SELECT (COUNT(*) > 0) FROM EntryStatusResponse WHERE entryId = :entryId and available = true ")
    public abstract boolean isEntryAvailableLocally(String entry);


    @UmQuery("")
    public abstract List<EntryStatusResponseWithNode> findByEntryIdAndAvailability(String entryId, boolean available);


}
