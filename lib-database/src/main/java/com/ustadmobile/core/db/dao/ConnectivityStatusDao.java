package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;

@UmDao
public abstract class ConnectivityStatusDao{

    @UmInsert
    public abstract long insert(ConnectivityStatus connectivityStatus);

    @UmQuery("UPDATE ConnectivityStatus SET connectivityState = :connectivityState")
    public abstract int update(int connectivityState);

    @UmQuery("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    public abstract UmLiveData<ConnectivityStatus> getStatusLive();



}
