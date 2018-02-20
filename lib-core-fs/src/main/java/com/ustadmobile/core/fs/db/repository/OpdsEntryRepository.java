package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsEntryRepository extends OpdsEntryWithRelationsDao {

    private DbManager dbManager;

    private ExecutorService executorService;

    public OpdsEntryRepository(DbManager dbManager, ExecutorService executorService){
        this.dbManager = dbManager;
        this.executorService = executorService;
    }

    //TODO: rename entryId parameter - it's misleading when it is really uuid
    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryId,
                                                            OpdsEntry.OpdsItemLoadCallback callback) {
        //get the result from the database
        UmLiveData<OpdsEntryWithRelations> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .getEntryByUrl(url, entryId, callback);
        OpdsEntryWithRelations entryWithRelations = new OpdsEntryWithRelations();
        entryWithRelations.setUuid(entryId);

        executorService.execute(new OpdsItemLoader(dbManager.getContext(), dbManager,
                entryWithRelations, url, callback));

        //execute something to check if required
        return dbResult;
    }

    @Override
    public OpdsEntryWithRelations getEntryByUrlStatic(String url) {
        return null;
    }

    @Override
    public UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        UmLiveData<List<OpdsEntryWithRelations>> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .findEntriesByContainerFileDirectoryAsList(dirList, callback);

        executorService.execute(new OpdsDirScanner(dbManager, dirList, callback));

        return dbResult;
    }

    @Override
    public UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        UmProvider<OpdsEntryWithRelations> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .findEntriesByContainerFileDirectoryAsProvider(dirList, callback);

        executorService.execute(new OpdsDirScanner(dbManager, dirList, callback));

        return dbResult;
    }

    public List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String containerFilePath) {
        OpdsDirScanner scanner = new OpdsDirScanner(dbManager);
        ContainerFileWithRelations containerFile = scanner.scanFile(new File(containerFilePath));

        return dbManager.getOpdsEntryWithRelationsDao().findEntriesByContainerFileNormalizedPath(
                containerFilePath);
    }


    @Override
    public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
        return null;
    }

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid) {
        return null;
    }

    @Override
    public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId) {
        return null;
    }

    @Override
    public String getUuidForEntryUrl(String url) {
        return null;
    }

    @Override
    public OpdsEntryWithRelations getEntryByUuidStatic(String uuid) {
        return null;
    }

    @Override
    public String findParentUrlByChildUuid(String childUuid) {
        return null;
    }

    @Override
    public int deleteOpdsEntriesByUuids(List<String> entryUuids) {
        return 0;
    }

    @Override
    public int deleteLinksByOpdsEntryUuids(List<String> entryUuids) {
        return 0;
    }

    @Override
    public List<OpdsEntryWithRelationsAndContainerMimeType> findByUuidsWithContainerMimeType(List<String> uuids) {
        return null;
    }

    @Override
    public List<OpdsEntryWithRelations> getEntriesByParentAsListStatic(String parentId) {
        return null;
    }
}
