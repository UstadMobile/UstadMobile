package com.ustadmobile.view

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

    private var parentUid: Long = 0

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        parentUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: MASTER_SERVER_ROOT_ENTRY_UID
    }


    override fun RBuilder.render() {
        if(parentUid != 0L){

           /* val libraryTab = UstadTab(ContentEntryList2View.VIEW_NAME,
                mapOf(ARG_PARENT_ENTRY_UID to parentUid, ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT),
                getString(MessageID.libraries))

            val downloadedTab = UstadTab(ContentEntryList2View.VIEW_NAME,
                mapOf(ARG_PARENT_ENTRY_UID to parentUid, ARG_CONTENT_FILTER to ARG_DOWNLOADED_CONTENT),
                getString(MessageID.downloaded))

            renderTabs(listOf(libraryTab, downloadedTab), false)*/
        }
    }
}