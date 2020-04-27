package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Role

interface RoleListView: UstadListView<Role, Role> {

    companion object {
        const val VIEW_NAME = "RoleListView"
    }

}