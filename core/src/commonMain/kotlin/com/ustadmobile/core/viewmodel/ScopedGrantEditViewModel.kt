package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.lib.db.entities.ScopedGrant

data class ScopedGrantEditUiState(

    val entity: ScopedGrant? = null,

    val bitmaskList: List<BitmaskFlag> = listOf()

)

class ScopedGrantEditViewModel  {

    companion object {

        const val DEST_NAME = "ScopedGrantEdit"

        const val ARG_GRANT_TO_GROUPUID = "grantToGroup"

        const val ARG_GRANT_TO_NAME = "grantToName"

        const val ARG_GRANT_ON_TABLE_ID = "grantTableId"

        const val ARG_GRANT_ON_ENTITY_UID = "grantEntityUid"

        //Table id (int) for the permission list to display. Which permissions are shown
        // (e.g. whether or not school related permissions are shown) is based on this.
        const val ARG_PERMISSION_LIST = "permissionList"

    }

}
