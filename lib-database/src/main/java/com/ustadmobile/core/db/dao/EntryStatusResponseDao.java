package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;

import java.util.List;

/**
 * Created by mike on 1/31/18.
 */
@UmDao
public abstract class EntryStatusResponseDao {

    @UmInsert
    public abstract void insert(List<EntryStatusResponse> responses);


    @UmQuery("SELECT (COUNT(*) > 0) FROM EntryStatusResponse WHERE entryId = :entryId and available = 1 ")
    public abstract boolean isEntryAvailableLocally(String entryId);


    @UmQuery("SELECT * FROM EntryStatusResponse " +
            " LEFT JOIN NetworkNode ON EntryStatusResponse.responderNodeId = NetworkNode.nodeId " +
            "WHERE entryId = :entryId AND available = :available ")
    public abstract List<EntryStatusResponseWithNode> findByEntryIdAndAvailability(String entryId, boolean available);


}
