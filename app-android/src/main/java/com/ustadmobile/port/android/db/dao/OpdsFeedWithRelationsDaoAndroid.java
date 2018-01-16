package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsFeedWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsFeedWithRelations;

/**
 * Created by mike on 1/14/18.
 */

@Dao
public abstract class OpdsFeedWithRelationsDaoAndroid extends OpdsFeedWithRelationsDao{

    @Query("Select * FROM opdsfeed WHERE url = :url")
    public abstract LiveData<OpdsFeedWithRelations> getFeedByUrlR(String url);

    @Override
    public UmLiveData<OpdsFeedWithRelations> getFeedByUrl(String url) {
        return new UmLiveDataAndroid(getFeedByUrlR(url));
    }
}
