package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
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
@Serializable
class PersonAuth() {

    @PrimaryKey(autoGenerate = true)
    var personAuthUid: Long = 0

    var passwordHash: String? = null

    var personAuthStatus: Int = 0

    constructor(personAuthUid: Long, passwordHash: String) : this() {
        this.personAuthUid = personAuthUid
        this.passwordHash = passwordHash
    }

    companion object {
        val STATUS_SENT = 1
        val STATUS_NOT_SENT = 0
    }
}