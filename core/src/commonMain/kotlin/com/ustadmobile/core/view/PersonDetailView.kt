package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazz
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzAndAttendance
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var clazzes: DataSource.Factory<Int, ClazzEnrolmentWithClazzAndAttendance>?

    var changePasswordVisible: Boolean

    var isAdmin: Boolean

    var showCreateAccountVisible: Boolean

    var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}