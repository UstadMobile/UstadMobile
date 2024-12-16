package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip
import com.ustadmobile.lib.db.entities.ClazzInvite

class CheckContactTypeUseCase(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
) {
    operator fun invoke(contact: String): InviteViaContactChip? {
        val validContact = if (validateEmailUseCase(contact) != null) {

            InviteViaContactChip(contact, true, ClazzInvite.EMAIL)

        } else if (phoneNumValidatorUseCase.isValid(contact)) {

            InviteViaContactChip(contact, true, ClazzInvite.PHONE)

        } else if (isValidUserName(contact)) {

            InviteViaContactChip(contact, true, ClazzInvite.INTERNAL_MESSAGE)

        } else {
            null
        }
        return validContact
    }

    private fun isValidUserName(username: String): Boolean {

        return username.contains("@")
    }
}