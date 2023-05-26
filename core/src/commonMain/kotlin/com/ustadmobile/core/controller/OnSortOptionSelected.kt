package com.ustadmobile.core.controller

import com.ustadmobile.core.util.SortOrderOption

fun interface OnSortOptionSelected {

    fun onClickSort(sortOption: SortOrderOption)

}