package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Holiday

interface HolidayEditView: UstadEditView<Holiday> {

    companion object {

        const val VIEW_NAME = "HolidayEditView"

    }

}