package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip


class ParseInviteUseCase(
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) {

    operator fun invoke(
        text: String
    ): List<InviteViaContactChip> {
        val parts = text.split(",").map { it.trim() }
        val validatedChips: MutableList<InviteViaContactChip> = mutableListOf()
        for (i in parts.indices) {
            val part = parts[i].trim() // Trim to remove any leading or trailing whitespace

            // Check if the part is a valid email or phone number
            if (validateEmailUseCase(part) != null || phoneNumValidatorUseCase.isValid(part) || isValidUserName(part)
            ) {
                validatedChips.add(InviteViaContactChip(part, true))
            } else {
                validatedChips.add(InviteViaContactChip(part, false))

            }
        }
        return validatedChips

    }


    private fun isValidUserName(username: String): Boolean {

        return username.contains("@")
    }
}