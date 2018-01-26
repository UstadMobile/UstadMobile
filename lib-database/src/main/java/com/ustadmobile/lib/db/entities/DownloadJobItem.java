package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

/**
 * Created by mike on 1/26/18.
 */
@UmEntity
public class DownloadJobItem {

    @UmPrimaryKey(autoIncrement = true)
    private int id;

    private int downloadJobId;

    private String entryId;

    private long updated;

    private int status;

    private int containerFileId;



}
