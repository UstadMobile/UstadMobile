package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class AccessToken {

    @UmPrimaryKey
    @PrimaryKey
    @NonNull
    var token: String? = null

    var accessTokenPersonUid: Long = 0

    var expires: Long = 0

    constructor()

    constructor(personUid: Long, expires: Long) {
        //TODO"Fix UUID to use expect / actual"
        //token = UUID.randomUUID().toString()
        token = ""
        this.accessTokenPersonUid = personUid
        this.expires = expires
    }
}
