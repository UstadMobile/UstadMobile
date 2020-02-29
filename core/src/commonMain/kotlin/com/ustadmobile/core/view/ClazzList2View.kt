package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.core.controller.ClazzList2Presenter
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

interface ClazzList2View: UstadView {

    var addButtonVisible: Boolean

    var sortOptions: List<ClazzList2Presenter.SortOrder>?

    var clazzList: DataSource.Factory<Int, ClazzWithNumStudents>?

    companion object {
        const val VIEW_NAME = "ClazzList2"
    }

}