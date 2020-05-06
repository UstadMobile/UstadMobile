package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails

interface ClazzDetailOverviewView: UstadDetailView<ClazzWithDisplayDetails> {

    companion object {

        const val VIEW_NAME = "ClazzDetailOverviewView"

    }

}