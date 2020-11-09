package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Category

interface CategoryListListener {

    fun onClickDelete(entry: Category)
    fun onClickCategory(entry: Category)

}