package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.dao.HttpCachedEntryDao;
import com.ustadmobile.lib.db.entities.HttpCachedEntry;

import java.util.List;

@Dao
public abstract class HttpCachedEntryDaoAndroid extends HttpCachedEntryDao {

    @Override
    @Query("SELECT * FROM HttpCachedEntry WHERE url = :url AND method = :method")
    public abstract HttpCachedEntry findByUrlAndMethod(String url, int method);

    @Override
    @Update
    public abstract void update(HttpCachedEntry entry);

    @Override
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(HttpCachedEntry entry);


    @Override
    @Query("SELECT fileUri FROM HttpCachedEntry WHERE url in (:urls)")
    public abstract List<String> findFileUrisByUrl(List<String> urls);

    @Override
    @Query("DELETE FROM HttpCachedEntry WHERE fileUri in (:deletedFileUris)")
    public abstract void deleteByFileUris(List<String> deletedFileUris);
}
