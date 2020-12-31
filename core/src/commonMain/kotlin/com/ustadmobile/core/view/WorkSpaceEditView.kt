package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage


interface WorkSpaceEditView: UstadEditView<WorkSpace> {

    var workspaceTermsList: DoorLiveData<List<WorkspaceTermsWithLanguage>>?

    companion object {

        const val VIEW_NAME = "WorkSpaceEditEditView"

    }

}