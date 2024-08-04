package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactChip
import com.ustadmobile.lib.db.entities.ClazzInvite


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
            if (validateEmailUseCase(part) != null) {

                validatedChips.add(InviteViaContactChip(part, true, ClazzInvite.EMAIL))

            } else if (phoneNumValidatorUseCase.isValid(part)) {

                validatedChips.add(InviteViaContactChip(part, true, ClazzInvite.PHONE))

            } else if (isValidUserName(part)) {

                validatedChips.add(InviteViaContactChip(part, true, ClazzInvite.INTERNAL_MESSAGE))

            } else {
                validatedChips.add(InviteViaContactChip(part, false, 0))

            }
        }
        return validatedChips

    }


    private fun isValidUserName(username: String): Boolean {

        return username.contains("@")
    }
}