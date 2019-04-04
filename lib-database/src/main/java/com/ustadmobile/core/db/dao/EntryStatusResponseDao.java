package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
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


    public static class EntryWithoutRecentResponse {

        private long containerUid;

        private int nodeId;

        public int getNodeId() {
            return nodeId;
        }

        public void setNodeId(int nodeId) {
            this.nodeId = nodeId;
        }

        public long getContainerUid() {
            return containerUid;
        }

        public void setContainerUid(long containerUid) {
            this.containerUid = containerUid;
        }
    }

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract Long[] insert(List<EntryStatusResponse> responses);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insert(EntryStatusResponse response);

    @UmQuery("DELETE FROM EntryStatusResponse")
    public abstract void deleteAll(UmCallback<Void> callback);

    @UmQuery("SELECT * FROM EntryStatusResponse " +
            " LEFT JOIN NetworkNode ON EntryStatusResponse.erNodeId = NetworkNode.nodeId " +
            "WHERE erContainerUid = :erContainerUid AND EntryStatusResponse.available = :available ")
    public abstract List<EntryStatusResponseWithNode> findByContainerUidAndAvailability(long erContainerUid, boolean available);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContainerUid =:erContainerUid AND erNodeId=:erNodeId")
    public abstract EntryStatusResponse findByContainerUidAndNetworkNode(long erContainerUid, long erNodeId);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContainerUid =:erContainerUid")
    public abstract EntryStatusResponse findByContainerUid(long erContainerUid);

    @UmQuery("SELECT * FROM EntryStatusResponse WHERE erContainerUid = :erContainerUid")
    public abstract UmLiveData<List<EntryStatusResponse>> getLiveEntryStatus(long erContainerUid);

    @UmQuery("SELECT Container.containerUid, NetworkNode.nodeId FROM Container, NetworkNode " +
            " WHERE Container.containerUid IN (:erContainerUids) " +
            " AND NetworkNode.nodeId IN (:nodeIds)  " +
            " AND NOT EXISTS(Select erId FROM EntryStatusResponse WHERE erContainerUid = Container.containerContentEntryUid" +
            " AND erNodeId = NetworkNode.nodeId AND responseTime > :sinceTime) ORDER BY NetworkNode.nodeId")
    public abstract List<EntryWithoutRecentResponse> findEntriesWithoutRecentResponse(
            List<Long> erContainerUids, List<Long> nodeIds, long sinceTime);
}
