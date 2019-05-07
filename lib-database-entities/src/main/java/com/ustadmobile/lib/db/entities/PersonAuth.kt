package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

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
@UmEntity(tableId = 30)
class PersonAuth {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var personAuthUid: Long = 0

    var passwordHash: String? = null

    @UmSyncLocalChangeSeqNum
    var personAuthLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var personAuthMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var lastChangedBy: Int = 0

    constructor()

    constructor(personAuthUid: Long, passwordHash: String) {
        this.personAuthUid = personAuthUid
        this.passwordHash = passwordHash
    }
}