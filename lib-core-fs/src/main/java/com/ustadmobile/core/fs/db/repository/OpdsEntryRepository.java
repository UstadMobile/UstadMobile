package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

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
    public UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(String dir) {
        UmLiveData<List<OpdsEntryWithRelations>> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .findEntriesByContainerFileDirectoryAsList(dir);

        executorService.execute(new OpdsDirScanner(dbManager, dir));

        return dbResult;
    }

    @Override
    public UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(String dir) {
        UmProvider<OpdsEntryWithRelations> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .findEntriesByContainerFileDirectoryAsProvider(dir);

        executorService.execute(new OpdsDirScanner(dbManager, dir));

        return dbResult;
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


}
