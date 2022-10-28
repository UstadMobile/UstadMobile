package com.ustadmobile.core.view

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint

interface GrantAppPermissionView: UstadView {


    fun onGranted(session: UserSessionWithPersonAndEndpoint)

    companion object {

        const val VIEW_NAME = "GrantAppPermission"

    }

}