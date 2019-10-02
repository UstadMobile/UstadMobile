package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = 48)
@Serializable
class PersonLocationJoin() {

    @PrimaryKey(autoGenerate = true)
    var personLocationUid: Long = 0

    var personLocationPersonUid: Long = 0

    var personLocationLocationUid: Long = 0

    @MasterChangeSeqNum
    var plMasterCsn: Long = 0

    @LocalChangeSeqNum
    var plLocalCsn: Long = 0

    @LastChangedBy
    var plLastChangedBy: Int = 0

    constructor(person: Person, location: Location) : this() {
        this.personLocationPersonUid = person.personUid
        this.personLocationLocationUid = location.locationUid
    }
}
