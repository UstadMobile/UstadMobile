package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ScopedGrantWithName : ScopedGrant() {

    var name: String? = null

}