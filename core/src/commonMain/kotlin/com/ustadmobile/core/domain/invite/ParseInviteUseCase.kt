package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip


class ParseInviteUseCase {
    var phoneNumValidatorUseCase: PhoneNumValidatorUseCase? = null
    operator fun invoke(
        text: String,
        phoneValidatorUseCase: PhoneNumValidatorUseCase
    ): List<InviteViaContactChip> {
        phoneNumValidatorUseCase = phoneValidatorUseCase
        val parts = text.split(",\\s*".toRegex())
        val validatedChips: MutableList<InviteViaContactChip> = mutableListOf()
        for (i in parts.indices) {
            val part = parts[i].trim() // Trim to remove any leading or trailing whitespace

            // Check if the part is a valid email or phone number
            if (isValidEmail(part) || isValidPhoneNumber(part) == true||isValidUserName(part)) {
                validatedChips.add(InviteViaContactChip(part, true))
            } else {
                validatedChips.add(InviteViaContactChip(part, false))

            }
        }
        return validatedChips

    }

    private fun isValidEmail(email: String): Boolean {
        ValidateEmailUseCase().invoke(email)
        return ValidateEmailUseCase().invoke(email) != null
    }

    private fun isValidPhoneNumber(phone: String): Boolean? {

        return phoneNumValidatorUseCase?.isValid(phone)
    }
    private fun isValidUserName(username: String): Boolean {

        return username.contains("@")
    }
}