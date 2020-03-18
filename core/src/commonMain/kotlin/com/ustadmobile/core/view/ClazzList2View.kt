package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents

interface ClazzList2View: UstadListView<Clazz, ClazzWithNumStudents> {

    companion object {
        const val VIEW_NAME = "ClazzList2"
    }

}