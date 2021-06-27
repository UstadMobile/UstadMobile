package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*

interface PersonDetailView: UstadDetailView<PersonWithPersonParentJoin> {

    var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>?

    var changePasswordVisible: Boolean

    var showCreateAccountVisible: Boolean

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}