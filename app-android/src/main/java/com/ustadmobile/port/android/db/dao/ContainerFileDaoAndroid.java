package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;

/**
 * Created by mike on 1/27/18.
 */

@Dao
public abstract class ContainerFileDaoAndroid extends ContainerFileDao {

    @Query("Select * From ContainerFile WHERE normalizedPath = :path ")
    public abstract ContainerFileWithRelations findContainerFilePathR(String path);

    @Override
    public ContainerFileWithRelations findContainerFileByPath(String dirPath){
        return findContainerFilePathR(dirPath);
    }

    @Override
    @Insert
    public abstract long insert(ContainerFile containerFile);

    @Override
    @Query("UPDATE ContainerFile SET lastUpdated = :lastUpdated WHERE id = :id")
    public abstract void updateLastUpdatedById(int id, long lastUpdated);
}
