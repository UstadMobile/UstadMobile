package com.ustadmobile.core.view

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Role


interface RoleEditView: UstadEditView<Role> {

    var permissionList : DoorLiveData<List<BitmaskFlag>>?

    companion object {

        const val VIEW_NAME = "RoleEditView"

    }

}