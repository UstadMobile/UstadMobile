package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class EntityRoleWithGroupName : EntityRole() {

    var groupName: String? = null
    var entityName: String? = null
    var entityType: String? = null
    var roleName: String? = null
    var clazzName: String? = null
    var locationName: String? = null
    var personName: String? = null
    var groupPersonName : String? = null
    var groupPersonUid : Long = 0L
}
