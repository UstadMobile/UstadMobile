package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Category


interface CategoryListItemListener {

    fun onClickCategory(category: Category)
    fun onClickRemove(category: Category)

}