package com.ustadmobile.core.viewmodel.systempermission

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.SystemPermissionDao
import kotlinx.coroutines.flow.flowOf

fun SystemPermissionDao.personHasSystemPermissionAsFlowForUser(
    accountPersonUid: Long,
    personUid: Long
) =  if(
    accountPersonUid != 0L && accountPersonUid == personUid
) {
    flowOf(true)
}else {
    personHasSystemPermissionAsFlow(
        accountPersonUid = accountPersonUid,
        permission = PermissionFlags.ALL
    )
}