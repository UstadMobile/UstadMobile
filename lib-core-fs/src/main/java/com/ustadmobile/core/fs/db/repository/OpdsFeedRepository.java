package com.ustadmobile.core.fs.db.repository;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsFeedWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsFeedWithRelations;

import java.util.concurrent.ExecutorService;

/**
 * Created by mike on 1/13/18.
 */

public class OpdsFeedRepository extends OpdsFeedWithRelationsDao {

    private DbManager dbManager;

    private ExecutorService executorService;

    public OpdsFeedRepository(DbManager dbManager, ExecutorService executorService){
        this.dbManager = dbManager;
        this.executorService = executorService;
    }

    @Override
    public UmLiveData<OpdsFeedWithRelations> getFeedByUrl(String url) {
        //get the result from the database
        UmLiveData<OpdsFeedWithRelations> dbResult = dbManager.getOpdsFeedWithRelationsDao().getFeedByUrl(url);
        executorService.execute(new OpdsItemLoader(dbManager.getContext(), dbManager,
                new OpdsFeedWithRelations(), url));

        //execute something to check if required
        return dbResult;
    }
}
