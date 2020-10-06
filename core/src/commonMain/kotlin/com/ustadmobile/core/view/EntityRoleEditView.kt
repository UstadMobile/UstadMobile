package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole

interface EntityRoleEditView: UstadEditView<EntityRoleWithNameAndRole> {

    var errorText: String?

    companion object {

        const val VIEW_NAME = "EntityRoleEdit"

    }

}