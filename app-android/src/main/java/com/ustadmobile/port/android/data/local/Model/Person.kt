package com.ustadmobile.port.android.data.local.Model

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Person(

    @PrimaryKey(autoGenerate = true)
    @field:SerializedName("personUid")
    var personUid: Long = 0,

    @field:SerializedName("username")
    var username: String? = null,

    @field:SerializedName("firstNames")
    var firstNames: String? = "",

    @field:SerializedName("lastName")
    var lastName: String? = "",

    @field:SerializedName("emailAddr")
    var emailAddr: String? = null,

    @field:SerializedName("phoneNum")
    var phoneNum: String? = null,

    @field:SerializedName("gender")
    var gender: Int = 0,

    @field:SerializedName("active")
    var active: Boolean = true,

    @field:SerializedName("admin")
    var admin: Boolean = false,

    @field:SerializedName("personNotes")
    var personNotes: String? = null,

    @field:SerializedName("fatherName")
    var fatherName: String? = null,

    @field:SerializedName("fatherNumber")
    var fatherNumber: String? = null,

    @field:SerializedName("motherName")
    var motherName: String? = null,

    @field:SerializedName("motherNum")
    var motherNum: String? = null,

    @field:SerializedName("dateOfBirth")
    var dateOfBirth: Long = 0,

    @field:SerializedName("personAddress")
    var personAddress: String? = null,

    // The ID given to the person by their organization
    @field:SerializedName("personOrgId")
    var personOrgId: String? = null,

    //The PersonGroup that is created for this individual
    @field:SerializedName("personGroupUid")
    var personGroupUid: Long = 0L,

    @field:SerializedName("personMasterChangeSeqNum")
    var personMasterChangeSeqNum: Long = 0,

    @field:SerializedName("personLocalChangeSeqNum")
    var personLocalChangeSeqNum: Long = 0,

    @field:SerializedName("personLastChangedBy")
    var personLastChangedBy: Int = 0,

    @field:SerializedName("personLct")
    var personLct: Long = 0,

    @field:SerializedName("personCountry")
    var personCountry: String? = null,

    @field:SerializedName("personType")
    var personType: Int = 0,

)