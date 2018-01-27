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

    @Query("Select * From ContainerFile WHERE dirPath = :dirPath")
    public abstract ContainerFileWithRelations findContainerFileByDirPathR(String dirPath);

    @Override
    public ContainerFileWithRelations findContainerFileByDirPath(String dirPath){
        return findContainerFileByDirPathR(dirPath);
    }

    @Override
    @Insert
    public abstract long insert(ContainerFile containerFile);
}
