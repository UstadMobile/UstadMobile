package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage


interface WorkspaceTermsEditView: UstadEditView<WorkspaceTermsWithLanguage> {

    companion object {

        const val VIEW_NAME = "WorkspaceTermsEditEditView"

    }

}