package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


interface WorkSpaceEditView: UstadEditView<WorkSpace> {

    var workspaceTermsList: DoorLiveData<List<SiteTermsWithLanguage>>?

    companion object {

        const val VIEW_NAME = "WorkSpaceEditEditView"

    }

}