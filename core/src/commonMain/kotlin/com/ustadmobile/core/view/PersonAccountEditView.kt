package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithAccount

interface PersonAccountEditView: UstadEditView<PersonWithAccount> {

    var currentPasswordRequiredErrorVisible: Boolean

    var newPasswordRequiredErrorVisible: Boolean

    var confirmedPasswordRequiredErrorVisible: Boolean

    var passwordDoNotMatchErrorVisible: Boolean

    var usernameRequiredErrorVisible: Boolean

    fun showErrorMessage(message: String,isPasswordError:Boolean)

    companion object {

        const val VIEW_NAME = "PersonAccountEditView"

    }

}