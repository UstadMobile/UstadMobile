package com.ustadmobile.lib.db.entities

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class AccessToken() {

    @PrimaryKey
    @NonNull
    var token: String = ""

    var accessTokenPersonUid: Long = 0

    var expires: Long = 0

    constructor(personUid: Long, expires: Long) : this() {
        token = ""
        this.accessTokenPersonUid = personUid
        this.expires = expires
    }

    constructor(personUid: Long, expires: Long, fToken:String) : this() {
        token = ""
        this.accessTokenPersonUid = personUid
        this.expires = expires
        this.token = fToken
    }
}
