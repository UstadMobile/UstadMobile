package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.lib.db.entities.NetworkNode;

import java.util.List;

/**
 * Created by mike on 1/30/18.
 */

@Dao
public abstract class NetworkNodeDaoAndroid extends NetworkNodeDao {

    @Override
    @Query("SELECT * From NetworkNode WHERE ipAddress = :ipAddress")
    public abstract NetworkNode findNodeByIpAddress(String ipAddress);

    @Query("Select * From NetworkNode Where ((ipAddress = :ipAddress AND ipAddress IS NOT NULL) OR (wifiDirectMacAddress = :wifiDirectMacAddress AND wifiDirectMacAddress IS NOT NULL))")
    public abstract NetworkNode findNodeByIpOrWifiDirectMacAddress(String ipAddress, String wifiDirectMacAddress);

    @Override
    @Query(findByBluetoothAddrSql)
    public abstract NetworkNode findNodeByBluetoothAddress(String bluetoothAddress);

    @Override
    @Insert
    public abstract long insert(NetworkNode node);

    @Override
    @Update
    public abstract void update(NetworkNode node);

    @Override
    @Query("Select * From NetworkNode")
    public abstract List<NetworkNode> findAllActiveNodes();
}
