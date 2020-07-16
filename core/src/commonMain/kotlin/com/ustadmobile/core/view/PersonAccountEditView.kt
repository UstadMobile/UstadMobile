package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person

interface PersonAccountEditView: UstadEditView<Person> {

    var currentPassword: String?

    var newPassword: String?

    var newPasswordRequiredErrorVisible: Boolean

    var currentPasswordRequiredErrorVisible: Boolean

    var usernameRequiredErrorVisible: Boolean

    var errorMessage:String?

    fun clearFields()

    companion object {

        const val VIEW_NAME = "PersonAccountEditView"

    }

}