package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>?

    var changePasswordVisible: Boolean

    var showCreateAccountVisible: Boolean

    var showConnectivityVisible: Boolean

    var homeConnectivityStatus: PersonConnectivity?

    var mobileConnectivityStatus: PersonConnectivity?

    var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}