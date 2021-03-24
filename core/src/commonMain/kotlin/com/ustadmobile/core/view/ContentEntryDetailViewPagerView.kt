package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface ContentEntryDetailViewPagerView : UstadDetailView<ContentEntry>{

    var tabs: List<String>?

    companion object {

        const val VIEW_NAME = "ContentEntryDetailViewPager"

    }
}