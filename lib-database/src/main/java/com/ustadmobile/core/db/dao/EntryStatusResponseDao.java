package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.EntryStatusResponseWithNode;

import java.util.List;

/**
 * Created by mike on 1/31/18.
 */
@UmDao
public abstract class EntryStatusResponseDao {

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract Long[] insert(List<EntryStatusResponse> responses);


    @UmQuery("SELECT (COUNT(*) > 0) FROM EntryStatusResponse WHERE entryId = :entryId and available = 1 ")
    public abstract boolean isEntryAvailableLocally(String entryId);


    @UmQuery("SELECT * FROM EntryStatusResponse " +
            " LEFT JOIN NetworkNode ON EntryStatusResponse.responderNodeId = NetworkNode.nodeId " +
            "WHERE entryId = :entryId AND available = :available ")
    public abstract List<EntryStatusResponseWithNode> findByEntryIdAndAvailability(String entryId, boolean available);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE entryid=:entryId AND responderNodeId=:nodeId")
    public abstract EntryStatusResponse findByEntryIdAndNetworkNode(long entryId, int nodeId);

    public static class EntryWithoutRecentResponse {
        private long contentEntryUid;

        private int nodeId;

        public long getContentEntryUid() {
            return contentEntryUid;
        }

        public void setContentEntryUid(long contentEntryUid) {
            this.contentEntryUid = contentEntryUid;
        }

        public int getNodeId() {
            return nodeId;
        }

        public void setNodeId(int nodeId) {
            this.nodeId = nodeId;
        }
    }

    @UmQuery("SELECT ContentEntry.contentEntryUid, NetworkNode.nodeId FROM ContentEntry, NetworkNode " +
            " WHERE ContentEntry.contentEntryUid IN (:contentUids) " +
            " AND NetworkNode.nodeId IN (:nodeIds)  " +
            " AND NOT EXISTS(Select id FROM EntryStatusResponse WHERE entryId = ContentEntry.contentEntryUid" +
            " AND responderNodeId = NetworkNode.nodeId) ORDER BY NetworkNode.nodeId")
    public abstract List<EntryWithoutRecentResponse> findEntriesWithoutRecentResponse(List<Long> contentUids, List<Integer> nodeIds);
}
