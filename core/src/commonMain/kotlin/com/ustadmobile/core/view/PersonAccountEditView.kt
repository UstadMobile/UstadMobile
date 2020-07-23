package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithAccount

interface PersonAccountEditView: UstadEditView<PersonWithAccount> {

    var currentPasswordError: String?

    var newPasswordError: String?

    var confirmedPasswordError: String?

    var noPasswordMatchError: String ?

    var usernameError: String?

    fun showErrorMessage(message: String,isPasswordError:Boolean)

    companion object {

        const val VIEW_NAME = "PersonAccountEditView"

    }

}