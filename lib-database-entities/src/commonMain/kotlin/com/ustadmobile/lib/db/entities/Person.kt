package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Person.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * Created by mike on 3/8/18.
 */

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
open class Person() {

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

    var personNotes: String? = null

    var personAddress: String? = null

    //Added speicifically for Goldozi : mPersonGroupUid is this person's managing group .
    //In Goldozi's case, a Lead Entrepreneur will be managing Women Embroider's group.
    var mPersonGroupUid : Long = 0L

    var fatherName: String? = null

    var fatherNumber: String? = null

    var motherName: String? = null

    var motherNum: String? = null

    var dateOfBirth: Long = 0

    //Added for simplicity for roles to a person directly for Goldozi.
    var personRoleUid : Long = 0L

    //Added for customers to be linked to actual Locaiton for Goldozi.
    var personLocationUid : Long = 0L

    @MasterChangeSeqNum
    var personMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var personLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var personLastChangedBy: Int = 0

    constructor(username: String, firstNames: String, lastName: String, active:Boolean = false,
                notes:String = "", address:String = "", phone:String = "") : this() {
        this.username = username
        this.firstNames = firstNames
        this.lastName = lastName
        this.active = active
        this.personNotes = notes
        this.personAddress = address
        this.phoneNum = phone
    }

    companion object {

        const val TABLE_ID = 9

        const val GENDER_UNSET = 0

        const val GENDER_FEMALE = 1

        const val GENDER_MALE = 2

        const val GENDER_OTHER = 4
    }
}
