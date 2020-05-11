package com.ustadmobile.core.view

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.lib.db.entities.Person

interface PersonEditView: UstadEditView<Person> {

    var genderOptions: List<MessageIdOption>?

    var personPicturePath: String?

    companion object {

        const val VIEW_NAME = "PersonEditView"

    }

}