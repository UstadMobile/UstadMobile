package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.@Entity@
@EditEntity_Import@

interface @BaseFileName@View: UstadEditView<@EditEntity@> {

    companion object {

        const val VIEW_NAME = "@BaseFileName@EditView"

    }

}