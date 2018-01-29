package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.impl.BaseUmCallback;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;

import java.util.concurrent.ExecutorService;

/**
 * Created by mike on 1/27/18.
 */

@Dao
public abstract class ContainerFileDaoAndroid extends ContainerFileDao implements UmDaoAndroid {

    private ExecutorService executor;

    @Override
    public void setExecutorService(ExecutorService executorService) {
        this.executor = executorService;
    }

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


    @Query("SELECT * From ContainerFile WHERE id = :containerFileId")
    public abstract ContainerFile getContainerFileEntryByIdR(int containerFileId);


    @Override
    public void getContainerFileByIdAsync(int containerFileId, BaseUmCallback<ContainerFile> callback){
        executor.execute(() -> callback.onSuccess(getContainerFileEntryByIdR(containerFileId)));
    }
}
