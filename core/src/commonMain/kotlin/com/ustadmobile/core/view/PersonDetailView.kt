package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithClazz
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails

interface PersonDetailView: UstadDetailView<PersonWithDisplayDetails> {

    var clazzes: DataSource.Factory<Int, ClazzEnrollmentWithClazz>?

    var changePasswordVisible: Boolean

    var showCreateAccountVisible: Boolean

    var rolesAndPermissions: DataSource.Factory<Int, EntityRoleWithNameAndRole>?

    companion object {

        const val VIEW_NAME = "PersonDetailView"

    }

}