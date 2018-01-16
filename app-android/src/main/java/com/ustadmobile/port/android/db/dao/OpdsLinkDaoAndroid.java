package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

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
}
