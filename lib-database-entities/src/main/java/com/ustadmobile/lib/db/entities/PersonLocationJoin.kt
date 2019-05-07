package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 48)
class PersonLocationJoin {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var personLocationUid: Long = 0

    var personLocationPersonUid: Long = 0

    var personLocationLocationUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var plMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var plLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var plLastChangedBy: Int = 0

    constructor()

    constructor(person: Person, location: Location) {
        this.personLocationPersonUid = person.personUid
        this.personLocationLocationUid = location.locationUid
    }
}
