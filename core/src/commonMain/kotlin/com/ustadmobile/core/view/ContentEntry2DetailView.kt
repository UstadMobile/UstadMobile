package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer


interface ContentEntry2DetailView: UstadDetailView<ContentEntryWithMostRecentContainer> {

    companion object {

        const val VIEW_NAME = "ContentEntryDetailView"

    }

}