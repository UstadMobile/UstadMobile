package com.ustadmobile.core.viewmodel.coursegroupset.edit

fun List<Int>.appendGroupNumIfNotInList(assignedGroupNum: Int) : List<Int>{
    return if((lastOrNull() ?: 0) < assignedGroupNum) {
        this + listOf(assignedGroupNum)
    }else {
        this
    }
}
