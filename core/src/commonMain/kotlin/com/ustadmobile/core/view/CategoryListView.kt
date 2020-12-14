package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Category


interface CategoryListView: UstadListView<Category, Category> {

    fun updateIsAdmin(isAdmin: Boolean)

    companion object {
        const val VIEW_NAME = "CategoryListView"
    }

}