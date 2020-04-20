package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Role


interface RoleEditView: UstadEditView<Role> {

    var permissionList : DoorLiveData<List<Role.BitmaskFlag>>

    companion object {

        const val VIEW_NAME = "RoleEditView"

    }

}