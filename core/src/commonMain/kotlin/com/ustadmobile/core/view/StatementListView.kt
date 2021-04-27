package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay


interface StatementListView: UstadListView<StatementWithSessionDetailDisplay, StatementWithSessionDetailDisplay> {

    var personWithContentTitle: String?

    companion object {

        const val VIEW_NAME = "StatementListView"

    }

}
