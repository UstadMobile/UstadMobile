package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person

interface PersonAccountEditView: UstadEditView<Person> {

    var firstPassword: String?

    var secondPassword: String?

    var secondPasswordFieldRequiredErrorVisible: Boolean

    var firstPasswordFieldRequiredErrorVisible: Boolean

    fun showPasswordDoNotMatchError()

    var usernameRequiredErrorVisible: Boolean

    var errorMessage:String?

    var fistPasswordFieldHint: String?

    var secondPasswordFieldHint: String?

    fun clearFields()

    companion object {

        const val VIEW_NAME = "PersonAccountEditView"

    }

}