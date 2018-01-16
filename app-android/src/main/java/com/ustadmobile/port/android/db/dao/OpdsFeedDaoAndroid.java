package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsFeedDao;
import com.ustadmobile.lib.db.entities.OpdsFeed;

/**
 * Created by mike on 1/14/18.
 */
@Dao
public abstract class OpdsFeedDaoAndroid extends OpdsFeedDao{

    @Query("Select * FROM OpdsFeed WHERE url = :url")
    public abstract LiveData<OpdsFeed> getFeedByUrlR(String url);

    @Override
    public final UmLiveData<OpdsFeed> getFeedByUrl(String url) {
        return new UmLiveDataAndroid<>(getFeedByUrlR(url));
    }

    @Override
    public long insert(OpdsFeed feed) {
        return insertR(feed);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertR(OpdsFeed feed);

    @Override
    @Update
    public abstract void update(OpdsFeed feed);
}
