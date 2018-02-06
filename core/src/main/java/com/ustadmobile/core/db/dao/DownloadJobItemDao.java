package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.db.entities.DownloadJobItem;

import java.util.List;

/**
 * Created by mike on 2/5/18.
 */

public abstract class DownloadJobItemDao {

    @UmInsert
    public abstract void insertList(List<DownloadJobItem> jobItems);

}
