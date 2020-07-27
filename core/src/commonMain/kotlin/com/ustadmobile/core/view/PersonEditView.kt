package com.ustadmobile.core.view

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount

interface PersonEditView: UstadEditView<PersonWithAccount> {

    var genderOptions: List<MessageIdOption>?

    var personPicturePath: String?

    var clazzList: DoorLiveData<List<ClazzMemberWithClazz>>?

    var classVisible: Boolean?

    var usernameError: String?

    var noMatchPasswordError: String?

    var passwordError: String?

    var confirmError: String?

    var errorMessage: String?

    fun navigateToNextDestination(account: UmAccount?, nextDestination: String)

    companion object {

        const val VIEW_NAME = "PersonEditView"

    }

}