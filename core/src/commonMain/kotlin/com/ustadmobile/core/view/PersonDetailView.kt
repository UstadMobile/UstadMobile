package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*

interface PersonDetailView: UstadDetailView<PersonWithPersonParentJoin> {

    var clazzes: DoorDataSourceFactory<Int, ClazzEnrolmentWithClazzAndAttendance>?

    var changePasswordVisible: Boolean

    var showCreateAccountVisible: Boolean

    var chatVisibility: Boolean

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}