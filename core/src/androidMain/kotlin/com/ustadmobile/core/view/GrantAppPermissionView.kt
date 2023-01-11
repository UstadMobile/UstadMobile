package com.ustadmobile.core.view

import android.graphics.drawable.Drawable

interface GrantAppPermissionView: UstadView {

    var grantToAppName: String?

    var grantToIcon: Drawable?

    companion object {

        const val VIEW_NAME = "GrantAppPermission"

        const val ARG_PERMISSION_UID = "eapUid"

        const val ARG_RETURN_NAME = "returnName"

    }

}