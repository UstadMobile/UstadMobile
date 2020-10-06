package com.ustadmobile.port.android.view

import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole

interface EntityRoleEditHandler {
    fun handleClickScope(entityRole: EntityRoleWithNameAndRole)
    fun handleClickRole(entityRole: EntityRoleWithNameAndRole)
}