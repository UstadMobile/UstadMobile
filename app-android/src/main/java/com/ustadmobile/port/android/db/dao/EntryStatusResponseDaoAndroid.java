package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;

import java.util.List;

/**
 * Created by mike on 1/31/18.
 */
@Dao
public abstract class EntryStatusResponseDaoAndroid extends EntryStatusResponseDao{


    @Override
    @Insert
    public abstract void insert(List<EntryStatusResponse> responses);

    @Override
    @Query("SELECT (COUNT(*) > 0) FROM EntryStatusResponse WHERE entryId = :entryId and available = 1 ")
    public abstract boolean isEntryAvailableLocally(String entryId);
}
