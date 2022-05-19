package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ScopedGrant

fun ScopedGrant.toBitmaskFlagList(): List<BitmaskFlag> {
    val tableAvailablePermissions = when(sgTableId) {
        Clazz.TABLE_ID -> ScopedGrantEditPresenter.COURSE_PERMISSIONS
        else -> 0L
    }

    return ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST.filter {
        tableAvailablePermissions.hasFlag(it.flagVal)
    }.map {
        it.toBitmaskFlag(sgPermissions)
    }
}