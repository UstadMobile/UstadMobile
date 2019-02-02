package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;

@UmDao
public abstract class ConnectivityStatusDao {

    @UmQuery("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    public abstract UmLiveData<ConnectivityStatus> getStatusLive();



}
