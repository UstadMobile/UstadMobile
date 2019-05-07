package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

import java.util.UUID

@UmEntity
@Entity
class AccessToken {

    @UmPrimaryKey
    @PrimaryKey
    var token: String? = null

    var accessTokenPersonUid: Long = 0

    var expires: Long = 0

    constructor()

    constructor(personUid: Long, expires: Long) {
        token = UUID.randomUUID().toString()
        this.accessTokenPersonUid = personUid
        this.expires = expires
    }
}
