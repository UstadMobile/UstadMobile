package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.List;

/**
 * Created by mike on 1/16/18.
 */
@Dao
public abstract class OpdsLinkDaoAndroid extends OpdsLinkDao{

    @Override
    @Insert
    public abstract void insert(List<OpdsLink> links);

    @Query("SELECT * From OpdsLink WHERE entryUuid = :entryUuid")
    public abstract List<OpdsLink> findLinkByEntryIdR(String entryUuid);

    @Override
    public List<OpdsLink> findLinkByEntryId(String entryId) {
        return findLinkByEntryIdR(entryId);
    }

    @Query("SELECT * FROM OpdsLink")
    public abstract List<OpdsLink> getAllLinksR();

    @Override
    public List<OpdsLink> getAllLinks() {
        return getAllLinksR();
    }
}
