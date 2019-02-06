package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;

import java.util.List;

/**
 * Created by mike on 1/27/18.
 */
@UmDao
public abstract class ContainerFileDao {

    @UmQuery("Select * From ContainerFile WHERE normalizedPath = :path ")
    public abstract ContainerFileWithRelations findContainerFileByPath(String path);

    @UmInsert
    public abstract long insert(ContainerFile containerFile);

    @UmQuery("UPDATE ContainerFile SET lastUpdated = :lastUpdated WHERE id = :id")
    public abstract void updateLastUpdatedById(int id, long lastUpdated);

    /**
     * Deletes a container file, and all entries associated with it (including ContainerFileEntry,
     * OpdsEntry, OpdsLink). This does *NOT* delete the file itself
     *
     * @param context
     * @param containerFile
     */
    public void deleteContainerFileAndRelations(Object context, ContainerFile containerFile) {
//        List<String> opdsEntryUuids = UmAppDatabase.getInstance(context).getContainerFileEntryDao()
//                .findOpdsEntryUuidsByContainerFileId(containerFile.getId());
//        List<String> opdsEntryIds = UmAppDatabase.getInstance(context).getContainerFileEntryDao()
//                .findEntryIdsByContainerFile(containerFile.getId());
//        UmAppDatabase.getInstance(context).getOpdsEntryWithRelationsDao().deleteEntriesWithRelationsByUuids(
//                opdsEntryUuids);
//        UmAppDatabase.getInstance(context).getContainerFileEntryDao().deleteByContainerFileId(containerFile.getId());
//        UmAppDatabase.getInstance(context).getOpdsEntryStatusCacheDao().handleContainerDeleted(opdsEntryIds);
//        delete(containerFile);
    }

    public void delete(ContainerFile containerFile) {
        System.out.println("ContainerFileDao.delete: " + containerFile.getId());
        Thread.dumpStack();
        deleteQuery(containerFile);
    }

    @UmDelete
    public abstract void deleteQuery(ContainerFile containerFile);

    @UmQuery("SELECT * From ContainerFile WHERE dirPath = :dirPath")
    public abstract List<ContainerFile> findFilesByDirectory(String dirPath);

    @UmQuery("Select * FROM ContainerFile")
    public abstract List<ContainerFile> findAll();
}
