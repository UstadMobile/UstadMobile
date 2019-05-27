package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

@UmEntity(tableId = 48)
@Entity
class PersonLocationJoin() {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var personLocationUid: Long = 0

    var personLocationPersonUid: Long = 0

    var personLocationLocationUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var plMasterCsn: Long = 0

    @UmSyncLocalChangeSeqNum
    var plLocalCsn: Long = 0

    @UmSyncLastChangedBy
    var plLastChangedBy: Int = 0

    constructor(person: Person, location: Location) : this() {
        this.personLocationPersonUid = person.personUid
        this.personLocationLocationUid = location.locationUid
    }
}
