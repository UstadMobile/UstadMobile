package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.Person.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * Created by mike on 3/8/18.
 */

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
class Person() {

    @PrimaryKey(autoGenerate = true)
    var personUid: Long = 0

    var username: String? = null

    var firstNames: String? = null

    var lastName: String? = null

    var emailAddr: String? = null

    var phoneNum: String? = null

    var gender: Int = 0

    var active: Boolean = false

    var admin: Boolean = false

    @MasterChangeSeqNum
    var personMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personLastChangedBy: Int = 0

    constructor(username: String, firstNames: String, lastName: String) : this() {
        this.username = username
        this.firstNames = firstNames
        this.lastName = lastName
    }

    companion object {

        const val TABLE_ID = 9

        const val GENDER_UNSET = 0

        const val GENDER_FEMALE = 1

        const val GENDER_MALE = 2

        const val GENDER_OTHER = 4
    }
}
