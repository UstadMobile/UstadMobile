package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Category


interface CategoryListView: UstadListView<Category, Category> {

    companion object {
        const val VIEW_NAME = "CategoryListView"
    }

}