package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.ContainerFileWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * This repository loads OpdsEntry objects using HTTP Atom Feeds as per the OPDS spec.
 */

public class OpdsAtomFeedRepositoryImpl implements OpdsAtomFeedRepository {

    private DbManager dbManager;

    private ExecutorService executorService;

    public OpdsAtomFeedRepositoryImpl(DbManager dbManager, ExecutorService executorService){
        this.dbManager = dbManager;
        this.executorService = executorService;
    }

    /**
     *
     * @param url
     * @param uuid
     * @param callback
     * @return
     */
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String uuid,
                                                            OpdsEntry.OpdsItemLoadCallback callback) {
        //get the result from the database
        UmLiveData<OpdsEntryWithRelations> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .getEntryByUrl(url, uuid, callback);
        OpdsEntryWithRelations entryWithRelations = new OpdsEntryWithRelations();
        entryWithRelations.setUuid(uuid);

        executorService.execute(new OpdsItemLoader(dbManager.getContext(), dbManager,
                entryWithRelations, url, callback));

        //execute something to check if required
        return dbResult;
    }

    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url) {
        return getEntryByUrl(url, null, null);
    }

    @Override
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String uuid) {
        return getEntryByUrl(url, uuid, null);
    }

    public OpdsEntryWithRelations getEntryByUrlStatic(String url) {
        OpdsEntryWithRelations entry = dbManager.getOpdsEntryWithRelationsDao()
                .getEntryByUrlStatic(url);

        if(entry != null) {
            return entry;//todo: check validity / http headers etc.
        }

        entry = new OpdsEntryWithRelations();
        OpdsItemLoader loader = new OpdsItemLoader(dbManager.getContext(), dbManager, entry, url, null);
        loader.run();

        return entry;
    }

    public UmLiveData<List<OpdsEntryWithStatusCache>> findEntriesByContainerFileDirectoryAsList(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        UmLiveData<List<OpdsEntryWithStatusCache>> dbResult = dbManager.getOpdsEntryWithRelationsDao()
                .findEntriesByContainerFileDirectoryAsList(dirList, callback);

        executorService.execute(new OpdsDirScanner(dbManager, dirList, callback));

        return dbResult;
    }

    public UmProvider<OpdsEntryWithStatusCache> findEntriesByContainerFileDirectoryAsProvider(
            List<String> dirList, OpdsEntry.OpdsItemLoadCallback callback) {
        UmProvider<OpdsEntryWithStatusCache> dbResult = dbManager.getOpdsEntryWithRelationsDao()
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

}
