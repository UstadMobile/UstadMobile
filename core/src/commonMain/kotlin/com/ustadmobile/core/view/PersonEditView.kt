package com.ustadmobile.core.view

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazz
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount

interface PersonEditView: UstadEditView<PersonWithAccount> {

    var genderOptions: List<MessageIdOption>?

    var personPicturePath: String?

    var clazzList: DoorLiveData<List<ClazzMemberWithClazz>>?

    var rolesAndPermissionsList: DoorLiveData<List<EntityRoleWithNameAndRole>>?

    var registrationMode: Boolean?

    var usernameError: String?

    var noMatchPasswordError: String?

    var passwordError: String?

    var confirmError: String?

    var errorMessage: String?

    var canDelegatePermissions: Boolean?

    fun navigateToNextDestination(account: UmAccount?, nextDestination: String)

    companion object {

        const val VIEW_NAME = "PersonEditView"

        /** This is a different view name that is mapped to a different NavController destination
         * This allows it to be recognized for purposes of controlling the visibility of the bottom
         * navigation bar
         */
        const val VIEW_NAME_REGISTER = "PersonEditRegisterView"

        /**
         * If true, the view will show space for the user to enter a username and password to register.
         * The presenter will then register the new user with the server (provided via ARG_SERVER_URL)
         */
        const val ARG_REGISTRATION_MODE = "RegMode"

    }

}