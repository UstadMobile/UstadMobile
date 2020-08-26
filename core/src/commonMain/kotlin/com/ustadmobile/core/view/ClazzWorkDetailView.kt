package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzWork


interface ClazzWorkDetailView: UstadDetailView<ClazzWork> {

    var isStudent : Boolean

    var ustadFragmentTitle: String?

    companion object {

        const val VIEW_NAME = "ClazzWorkDetailView"

    }

}