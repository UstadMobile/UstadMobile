package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Clazz


interface ClazzDetailView: UstadDetailView<Clazz> {

    var tabs: List<String>?

    companion object {

        const val VIEW_NAME = "CourseDetailView"

        const val ARG_TABS = "courseTabs"

    }

}