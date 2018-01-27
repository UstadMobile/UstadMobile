package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.lib.db.entities.ContainerFileEntry;

import java.util.List;

/**
 * Created by mike on 1/27/18.
 */

@Dao
public abstract class ContainerFileEntryDaoAndroid extends ContainerFileEntryDao{

    @Insert
    public abstract void insert(List<ContainerFileEntry> fileEntries);
}
