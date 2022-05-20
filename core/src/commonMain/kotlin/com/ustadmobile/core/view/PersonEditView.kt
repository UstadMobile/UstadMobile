package com.ustadmobile.core.view

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.PersonWithAccount
import com.ustadmobile.lib.db.entities.UmAccount

interface PersonEditView: UstadEditView<PersonWithAccount> {

    var genderOptions: List<MessageIdOption>?

    var personPicture: PersonPicture?

    /**
     * This is set only when registering a minor
     */
    var approvalPersonParentJoin: PersonParentJoin?

    var registrationMode: Int

    var usernameError: String?

    var noMatchPasswordError: String?

    var passwordError: String?

    var emailError: String?

    var confirmError: String?

    var dateOfBirthError: String?

    var parentContactError: String?

    var firstNamesFieldError: String?
    var lastNameFieldError: String?
    var genderFieldError: String?

    var firstNameError: String?

    var lastNameError: String?

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
         * If the form is in registration mode, then the date of birth must be supplied as an
         * argument.
         */
        const val ARG_DATE_OF_BIRTH = "DateOfBirth"

        /**
         * If this is set then this means that the person registering has come from a link. Since someone in the system has invited another person
         * we use this flag to remove the age restrictions of being under 13 to sign up.
         */
        const val REGISTER_VIA_LINK = "RegViaLink"

        /**
         * Registration mode argument value indicating that this is not being used in registration mode
         */
        const val REGISTER_MODE_NONE = 0

        /**
         * Registration mode argument value indicating that this is being used to register a user
         * who is not a minor (age > 13)
         */
        const val REGISTER_MODE_ENABLED = 1

        /**
         * Registration mode argument value indicating that a minor is being registered
         */
        const val REGISTER_MODE_MINOR = 2


    }

}