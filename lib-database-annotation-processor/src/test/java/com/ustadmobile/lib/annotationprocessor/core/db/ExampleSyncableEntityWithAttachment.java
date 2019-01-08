package com.ustadmobile.lib.annotationprocessor.core.db;

import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

public class ExampleSyncableEntityWithAttachment {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long eseUid;

    @UmSyncMasterChangeSeqNum
    private long eseLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long eseMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int eseLastChangedBy;

    private String filename;

    private String mimeType;

}
