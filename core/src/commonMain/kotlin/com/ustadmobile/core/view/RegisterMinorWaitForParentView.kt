package com.ustadmobile.core.view

interface RegisterMinorWaitForParentView : UstadView {

    var username: String?

    var password: String?

    var parentContact: String?

    var passwordVisible: Boolean

    companion object {

        const val ARG_USERNAME = "username"

        const val ARG_PASSWORD = "password"

        const val ARG_PARENT_CONTACT = "parentContact"

        const val VIEW_NAME = "RegisterMinorWaitForParent"

    }

}