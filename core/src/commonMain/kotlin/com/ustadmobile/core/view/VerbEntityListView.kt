package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.VerbDisplay


interface VerbEntityListView: UstadListView<VerbDisplay, VerbDisplay> {

    companion object {
        const val VIEW_NAME = "VerbEntityListView"

        const val ARG_EXCLUDE_VERBUIDS_LIST = "excludeAlreadySelectedList"
    }

}