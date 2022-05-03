package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails

interface ClazzList2View: UstadListView<Clazz, ClazzWithListDisplayDetails> {

    var newClazzListOptionVisible: Boolean

    companion object {
        const val VIEW_NAME = "Courses"

        const val VIEW_NAME_HOME = "CoursesHome"

        const val ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST = "excludeAlreadySelectedClazzList"
    }

}