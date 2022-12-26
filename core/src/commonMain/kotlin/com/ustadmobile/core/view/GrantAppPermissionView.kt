package com.ustadmobile.core.view

interface GrantAppPermissionView: UstadView {



    companion object {

        const val VIEW_NAME = "GrantAppPermission"

        const val ARG_PERMISSION_UID = "eapUid"

        const val ARG_GRANT_PERMISSION_CALLER_UID = "callerUid"

        const val ARG_APPROVAL_URI = "approvalUri"

    }

}