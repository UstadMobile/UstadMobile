package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 1/30/18.
 */
@UmDao
public abstract class NetworkNodeDao {

    @UmQuery("SELECT * From NetworkNode WHERE ipAddress = :ipAddress")
    public abstract NetworkNode findNodeByIpAddress(String ipAddress);

    @UmQuery("SELECT * From NetworkNode WHERE nodeId = :nodeId")
    public abstract NetworkNode findNodeById(long nodeId);

    @UmQuery("Select * From NetworkNode Where ((ipAddress = :ipAddress AND ipAddress IS NOT NULL) OR (wifiDirectMacAddress = :wifiDirectMacAddress AND wifiDirectMacAddress IS NOT NULL))")
    public abstract NetworkNode findNodeByIpOrWifiDirectMacAddress(String ipAddress, String wifiDirectMacAddress);

    protected static final String findByBluetoothAddrSql = "SELECT * from NetworkNode WHERE bluetoothMacAddress = :bluetoothAddress";

    @UmQuery(findByBluetoothAddrSql)
    public abstract NetworkNode findNodeByBluetoothAddress(String bluetoothAddress);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insert(NetworkNode node);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract Long [] insert(List<NetworkNode> nodeList);

    @UmUpdate
    public abstract void update(NetworkNode node);

    @UmQuery("DELETE FROM NetworkNode")
    public abstract void deleteAll(UmCallback<Void> callback);

    @UmQuery("SELECT * FROM NetworkNode")
    public abstract List<NetworkNode> getAll();

    @UmQuery("UPDATE NetworkNode SET numFailureCount = numFailureCount + 1 WHERE nodeId = :nodeId")
    public abstract void updateRetryCount(long nodeId, UmCallback<Void> callback);

    @UmQuery("Select * From NetworkNode WHERE lastUpdateTimeStamp >= :lastUpdatedTime" +
            " AND numFailureCount <= :maxNumFailure")
    public abstract List<NetworkNode> findAllActiveNodes(long lastUpdatedTime, int maxNumFailure);

    @ UmQuery ("UPDATE NetworkNode set lastUpdateTimeStamp = :lastUpdateTimeStamp WHERE bluetoothMacAddress = :bluetoothAddress")
    public abstract void updateLastSeen(String bluetoothAddress,long lastUpdateTimeStamp, UmCallback<Integer> numChanged);

    @UmQuery("SELECT NetworkNode.* FROM NetworkNode " +
            "LEFT JOIN EntryStatusResponse ON NetworkNode.nodeId = EntryStatusResponse.erNodeId " +
            "WHERE EntryStatusResponse.erContainerUid = :containerUid " +
            "AND EntryStatusResponse.available " +
            "AND NetworkNode.lastUpdateTimeStamp > :minLastSeenTimestamp " +
            "AND (Select COUNT(*) FROM DownloadJobItemHistory " +
            "WHERE DownloadJobItemHistory.networkNode = NetworkNode.nodeId " +
            "AND NOT successful AND startTime > :maxFailuresFromTimestamp) < :maxFailuresInPeriod " +
            "LIMIT 1")
    public abstract NetworkNode findLocalActiveNodeByContainerUid(long containerUid,
                                                                  long minLastSeenTimestamp,
                                                                  int maxFailuresInPeriod,
                                                                  long maxFailuresFromTimestamp);


    @UmQuery("UPDATE NetworkNode SET groupSsid = :groupSsid, endpointUrl = :endpointUrl  WHERE nodeId = :nodeId")
    public abstract void updateNetworkNodeGroupSsid(long nodeId, String groupSsid, String endpointUrl);

    @UmQuery("SELECT endpointUrl FROM NetworkNode WHERE groupSsid = :ssid")
    public abstract String getEndpointUrlByGroupSsid(String ssid);

}
