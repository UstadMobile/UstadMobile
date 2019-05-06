package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

import java.util.UUID

@UmEntity
@Entity
open class AccessToken(
        @field:PrimaryKey
        @field:UmPrimaryKey
        var token: String = UUID.randomUUID().toString(),

        var accessTokenPersonUid: Long = 0,

        var expires: Long = 0
        ) {


    constructor() : this(UUID.randomUUID().toString(), 0, 0)

}
