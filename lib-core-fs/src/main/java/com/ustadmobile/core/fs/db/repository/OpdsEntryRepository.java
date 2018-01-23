package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
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
    public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url) {
        //get the result from the database
        UmLiveData<OpdsEntryWithRelations> dbResult = dbManager.getOpdsEntryWithRelationsDao().getEntryByUrl(url);
        executorService.execute(new OpdsItemLoader(dbManager.getContext(), dbManager,
                new OpdsEntryWithRelations(), url));

        //execute something to check if required
        return dbResult;
    }

    @Override
    public UmProvider<OpdsEntryWithRelations> findEntriesByFeed(String feedId) {
        return null;
    }

    @Override
    public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
        return null;
    }
}
