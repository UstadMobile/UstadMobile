package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
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

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insert(EntryStatusResponse response);



    @UmQuery("SELECT * FROM EntryStatusResponse " +
            " LEFT JOIN NetworkNode ON EntryStatusResponse.erNodeId = NetworkNode.nodeId " +
            "WHERE erContentEntryFileUid = :erContentEntryFileUid AND EntryStatusResponse.available = :available ")
    public abstract List<EntryStatusResponseWithNode> findByEntryIdAndAvailability(long erContentEntryFileUid, boolean available);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContentEntryFileUid =:erContentEntryFileUid AND erNodeId=:erNodeId")
    public abstract EntryStatusResponse findByEntryIdAndNetworkNode(long erContentEntryFileUid, long erNodeId);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContentEntryFileUid =:erContentEntryFileUid")
    public abstract EntryStatusResponse findByContentEntryFileUid(long erContentEntryFileUid);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContentEntryFileUid = :erContentEntryFileUid")
    public abstract UmLiveData<EntryStatusResponse> getLiveEntryStatus(long erContentEntryFileUid);


    public static class EntryWithoutRecentResponse {
        private long entryId;

        private int nodeId;

        public int getNodeId() {
            return nodeId;
        }

        public long getEntryId() {
            return entryId;
        }

        public void setEntryId(long entryId) {
            this.entryId = entryId;
        }

        public void setNodeId(int nodeId) {
            this.nodeId = nodeId;
        }
    }

    @UmQuery("SELECT ContentEntry.entryId, NetworkNode.nodeId FROM ContentEntry, NetworkNode " +
            " WHERE ContentEntry.contentEntryUid IN (:contentUids) " +
            " AND NetworkNode.nodeId IN (:nodeIds)  " +
            " AND NOT EXISTS(Select erId FROM EntryStatusResponse WHERE erContentEntryFileUid = ContentEntry.contentEntryUid" +
            " AND erNodeId = NetworkNode.nodeId) ORDER BY NetworkNode.nodeId")
    public abstract List<EntryWithoutRecentResponse> findEntriesWithoutRecentResponse(List<Long> contentUids, List<Long> nodeIds);
}
