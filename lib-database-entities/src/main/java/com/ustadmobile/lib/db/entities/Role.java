package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

@UmEntity(tableId = 45)
public class Role {

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long roleUid;

    private String roleName;

    @UmSyncMasterChangeSeqNum
    private long roleMasterCsn;

    @UmSyncMasterChangeSeqNum
    private long roleLocalCsn;

    @UmSyncLastChangedBy
    private long roleLastChangedBy;


}
