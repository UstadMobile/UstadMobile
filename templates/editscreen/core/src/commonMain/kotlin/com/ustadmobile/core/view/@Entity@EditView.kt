package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@

interface @Entity@EditView: UstadEditView<@EditEntity@> {

    companion object {

        const val VIEW_NAME = "@Entity@EditView"

    }

}