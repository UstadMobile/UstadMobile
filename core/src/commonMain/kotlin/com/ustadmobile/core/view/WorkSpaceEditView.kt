package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.WorkSpace


interface WorkSpaceEditView: UstadEditView<WorkSpace> {

    companion object {

        const val VIEW_NAME = "WorkSpaceEditEditView"

    }

}