package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;

@UmDao
public abstract class ConnectivityStatusDao{

    @UmInsert (onConflict = UmOnConflictStrategy.REPLACE)
    public abstract void insert(ConnectivityStatus connectivityStatus, UmCallback<Long> callback);

    @UmInsert(onConflict =  UmOnConflictStrategy.REPLACE)
    public abstract void insert(ConnectivityStatus connectivityStatus);

    @UmQuery("UPDATE ConnectivityStatus SET connectivityState = :connectivityState")
    public abstract void updateState(int connectivityState, UmCallback<Void> callback);

    @UmQuery("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    public abstract void updateState(int connectivityState, String wifiSsid, UmCallback<Void> callback);

    @UmQuery("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    public abstract void updateStateSync(int connectivityState, String wifiSsid);

    public void addConnectivityStatusRecord(int state, String wifiSsid, boolean connectedOrConnecting,
                                            UmCallback<Void> callback){
        ConnectivityStatus connectivityStatus = new ConnectivityStatus();
        connectivityStatus.setConnectedOrConnecting(connectedOrConnecting);
        connectivityStatus.setConnectivityState(state);
        connectivityStatus.setWifiSsid(wifiSsid);
        insert(connectivityStatus,null);
    }

    @UmQuery("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    public abstract UmLiveData<ConnectivityStatus> getStatusLive();

    @UmQuery("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    public abstract ConnectivityStatus getStatus();
}
