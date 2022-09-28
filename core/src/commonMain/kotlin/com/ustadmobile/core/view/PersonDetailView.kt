package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*

interface PersonDetailView: UstadDetailView<PersonWithPersonParentJoin> {

    var clazzes: DataSourceFactory<Int, ClazzEnrolmentWithClazzAndAttendance>?

    var changePasswordVisible: Boolean

    var showCreateAccountVisible: Boolean

    var chatVisibility: Boolean

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}