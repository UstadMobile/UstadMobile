package com.ustadmobile.core.view

import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*

interface PersonEditView: UstadEditView<PersonWithAccount> {

    var genderOptions: List<MessageIdOption>?

    var connectivityStatusOptions: List<MessageIdOption>?

    var personPicturePath: String?

    var personPicture: PersonPicture?

    var clazzList: DoorLiveData<List<ClazzEnrolmentWithClazz>>?

    var rolesAndPermissionsList: DoorLiveData<List<EntityRoleWithNameAndRole>>?

    var registrationMode: Boolean?

    var usernameError: String?

    var noMatchPasswordError: String?

    var passwordError: String?

    var confirmError: String?

    var dateOfBirthError: String?

    var canDelegatePermissions: Boolean

    var viewConnectivityPermission: Boolean

    var firstNameError: String?

    var lastNameError: String?

    var countryError: String?

    var homeConnectivityStatusError: String?

    var mobileConnectivityStatusError: String?

    var homeConnectivityStatus: PersonConnectivity?

    var mobileConnectivityStatus: PersonConnectivity?

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

        /**
         * If this is set then this means that the person registering has come from a link. Since someone in the system has invited another person
         * we use this flag to remove the age restrictions of being under 13 to sign up.
         */
        const val REGISTER_VIA_LINK = "RegViaLink"

        const val ARG_HOME_ACCESS = "homeAccess"

        const val ARG_MOBILE_ACCESS = "mobileAccess"

    }

}