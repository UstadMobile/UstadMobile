package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.Category

interface CategoryChipListener {

    fun onClickEntry(entry: Category)

}