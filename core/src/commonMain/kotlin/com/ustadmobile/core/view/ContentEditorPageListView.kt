package com.ustadmobile.core.view

import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem

interface ContentEditorPageListView : UstadView {

    fun updatePageList(pageList: MutableList<EpubNavItem>, selectedPage: String?)

    fun setDocumentTitle(title: String)

    fun showAddOrUpdatePageDialog(page: EpubNavItem?, newPage: Boolean)

    fun dismissDialog()

    companion object{
        const val VIEW_NAME = "ContentPageList"
    }

}
