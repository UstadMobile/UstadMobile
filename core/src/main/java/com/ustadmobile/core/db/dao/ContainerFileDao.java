package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.impl.BaseUmCallback;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;

import java.util.List;

/**
 * Created by mike on 1/27/18.
 */

public abstract class ContainerFileDao {

    public abstract ContainerFileWithRelations findContainerFileByPath(String path);

    public abstract long insert(ContainerFile containerFile);

    public abstract void updateLastUpdatedById(int id, long lastUpdated);

    public abstract void getContainerFileByIdAsync(int containerFileId, BaseUmCallback<ContainerFile> callback);

    public abstract ContainerFileWithRelations getContainerFileById(int id);

    /**
     * Deletes a container file, and all entries associated with it (including ContainerFileEntry,
     * OpdsEntry, OpdsLink). This does *NOT* delete the file itself
     *
     * @param context
     * @param containerFile
     */
    public void deleteContainerFileAndRelations(Object context, ContainerFile containerFile) {
        List<String> opdsEntryUuids = DbManager.getInstance(context).getContainerFileEntryDao()
                .findOpdsEntryUuidsByContainerFileId(containerFile.getId());
        List<String> opdsEntryIds = DbManager.getInstance(context).getContainerFileEntryDao()
                .findEntryIdsByContainerFile(containerFile.getId());
        DbManager.getInstance(context).getOpdsEntryWithRelationsDao().deleteEntriesWithRelationsByUuids(
                opdsEntryUuids);
        DbManager.getInstance(context).getContainerFileEntryDao().deleteByContainerFileId(containerFile.getId());
        DbManager.getInstance(context).getOpdsEntryStatusCacheDao().handleContainerDeleted(opdsEntryIds);
        delete(containerFile);
    }

    @UmDelete
    public abstract void delete(ContainerFile containerFile);

    public abstract List<ContainerFile> findFilesByDirectory(String dirPath);

    @UmQuery("SELECT fileSize FROM ContainerFile WHERE id = :containerFileId")
    public abstract void findContainerFileLengthAsync(int containerFileId, UmCallback<Long> callback);

    @UmQuery("Select * FROM ContainerFile")
    public abstract List<ContainerFile> findAll();
}
