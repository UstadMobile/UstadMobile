package com.ustadmobile.lib.db.entities

import android.support.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class AccessToken() {

    @UmPrimaryKey
    @PrimaryKey
    @NonNull
    var token: String? = ""

    var accessTokenPersonUid: Long = 0

    var expires: Long = 0

    constructor(personUid: Long, expires: Long) : this() {
        //TODO"Fix UUID to use expect / actual"
        //token = UUID.randomUUID().toString()
        token = ""
        this.accessTokenPersonUid = personUid
        this.expires = expires
    }
}
