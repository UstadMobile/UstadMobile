package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsFeedWithRelations;

/**
 * Created by mike on 1/14/18.
 */

public abstract class OpdsFeedWithRelationsDao {

    @UmQuery("Select * From \"opds_feed\" WHERE \"url\" = :url ")
    public abstract UmLiveData<OpdsFeedWithRelations> getFeedByUrl(String url);
}
