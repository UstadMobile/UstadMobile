package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import kotlinx.serialization.Serializable

@Serializable
class EntityRoleWithNameAndRole : EntityRole() {

    @Embedded
    var entityRoleRole: Role? = null

    var entityRoleScopeName: String? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EntityRoleWithNameAndRole

        if (entityRoleRole != other.entityRoleRole) return false
        if (entityRoleScopeName != other.entityRoleScopeName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityRoleRole?.hashCode() ?: 0
        result = 31 * result + (entityRoleScopeName?.hashCode() ?: 0)
        return result
    }


}
