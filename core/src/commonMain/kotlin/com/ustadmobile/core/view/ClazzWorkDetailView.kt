package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzWork


interface ClazzWorkDetailView: UstadDetailView<ClazzWork> {

    fun setEditVisible(visible: Boolean)

    var studentRole : Boolean

    var title: String?

    companion object {

        const val VIEW_NAME = "ClazzWorkDetailView"

    }

}