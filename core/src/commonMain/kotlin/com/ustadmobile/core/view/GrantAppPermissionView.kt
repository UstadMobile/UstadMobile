package com.ustadmobile.core.view

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint

interface GrantAppPermissionView: UstadView {


    fun onGranted(session: UserSessionWithPersonAndEndpoint)

    companion object {

        const val VIEW_NAME = "GrantAppPermission"

        const val ARG_GRANT_PERMISSION_CALLER_UID = "callerUid"

        const val ARG_APPROVAL_URI = "approvalUri"

    }

}