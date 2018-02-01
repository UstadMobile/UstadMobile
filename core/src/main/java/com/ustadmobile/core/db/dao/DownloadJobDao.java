package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJob;

/**
 * Created by mike on 1/31/18.
 */

public abstract class DownloadJobDao {

    @UmInsert
    public abstract long insert(DownloadJob job);

}
