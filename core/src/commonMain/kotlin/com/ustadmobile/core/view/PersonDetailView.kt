package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var clazzes: DataSource.Factory<Int, ClazzMemberWithClazz>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}