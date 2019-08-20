package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

/**
 * This is a 1:1 relationship with Person. It avoids synchronizing login credentials with any other
 * devices in cases where another user has permission to view someone else's profile.
 *
 * There is no foreign key field, personAuthUid simply equals personUid.
 *
 * Note: this entity has change sequence numbers as it may be sync'd with particular, authorized
 * devices to provide a local login service.
 *
 * Currently, as PersonAuthDao does not extend syncable dao, it will not sync
 */
@Entity
@SyncableEntity(tableId = 30)
class PersonAuth() {

    @PrimaryKey(autoGenerate = true)
    var personAuthUid: Long = 0

    var passwordHash: String? = null

    @LocalChangeSeqNum
    var personAuthLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var personAuthMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var lastChangedBy: Int = 0

    constructor(personAuthUid: Long, passwordHash: String) : this() {
        this.personAuthUid = personAuthUid
        this.passwordHash = passwordHash
    }
}