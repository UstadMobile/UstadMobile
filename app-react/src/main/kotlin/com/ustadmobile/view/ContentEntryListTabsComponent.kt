package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.MASTER_SERVER_ROOT_ENTRY_UID
import react.RBuilder
import react.RProps
import react.RState

class ContentEntryListTabsComponent(mProps: RProps) :UstadBaseComponent<RProps, RState>(mProps),
    ContentEntryListTabsView {

    override val viewName: String
        get() = ContentEntryListTabsView.VIEW_NAME


    override fun RBuilder.render() {
        if(::arguments.isInitialized){
            val parentUid = (arguments[ARG_PARENT_ENTRY_UID] ?: MASTER_SERVER_ROOT_ENTRY_UID.toString())

            val libraryTab = UstadTab(ContentEntryList2View.VIEW_NAME,
                mapOf(ARG_PARENT_ENTRY_UID to parentUid, ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT),
                getString(MessageID.libraries))

            val downloadedTab = UstadTab(ContentEntryList2View.VIEW_NAME,
                mapOf(ARG_PARENT_ENTRY_UID to parentUid, ARG_CONTENT_FILTER to ARG_DOWNLOADED_CONTENT),
                getString(MessageID.downloaded))

            renderTabs(listOf(libraryTab, downloadedTab), false)
        }
    }
}