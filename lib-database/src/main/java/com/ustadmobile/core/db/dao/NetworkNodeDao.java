package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * Created by mike on 1/30/18.
 */
@UmDao
public abstract class NetworkNodeDao {

    @UmQuery("SELECT * From NetworkNode WHERE ipAddress = :ipAddress")
    public abstract NetworkNode findNodeByIpAddress(String ipAddress);

    @UmQuery("SELECT * From NetworkNode WHERE nodeId = :nodeId")
    public abstract NetworkNode findNodeById(int nodeId);

    @UmQuery("Select * From NetworkNode Where ((ipAddress = :ipAddress AND ipAddress IS NOT NULL) OR (wifiDirectMacAddress = :wifiDirectMacAddress AND wifiDirectMacAddress IS NOT NULL))")
    public abstract NetworkNode findNodeByIpOrWifiDirectMacAddress(String ipAddress, String wifiDirectMacAddress);

    protected static final String findByBluetoothAddrSql = "SELECT * from NetworkNode WHERE bluetoothMacAddress = :bluetoothAddress";

    @UmQuery(findByBluetoothAddrSql)
    public abstract NetworkNode findNodeByBluetoothAddress(String bluetoothAddress);

    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insert(NetworkNode node);

    @UmUpdate
    public abstract void update(NetworkNode node);

    @UmQuery("Select * From NetworkNode")
    public abstract List<NetworkNode> findAllActiveNodes();

    @ UmQuery ("UPDATE NetworkNode set lastUpdateTimeStamp = :lastUpdateTimeStamp WHERE bluetoothMacAddress = :bluetoothAddress")
    public abstract void updateLastSeen(String bluetoothAddress,long lastUpdateTimeStamp, UmCallback<Integer> numChanged);

}
