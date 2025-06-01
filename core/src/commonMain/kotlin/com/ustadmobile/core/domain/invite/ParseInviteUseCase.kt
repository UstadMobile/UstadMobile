package com.ustadmobile.core.domain.invite

import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.viewmodel.clazz.inviteviacontact.InviteViaContactChip
import com.ustadmobile.lib.db.entities.ClazzInvite

/**
 * Separates out a string (e.g. text field input) into contacts - where the contacts can be
 * separated by whitespace, a comma, or a semicolon. Typically invoked when the user submits
 * contacts into a text field where they are turned chips.
 */
class ParseInviteUseCase(
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
) {

    /**
     * @param text user entered text to parse into contacts
     * @return list of InviteViaContactChip parsed from teh string
     */
    operator fun invoke(
        text: String
    ): List<InviteViaContactChip> {
        //Regex: accepts whitespace, comma, or semicolon
        val parts = text.split(Regex("\\s+|,|;"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        return parts.map { part ->
            // Check if the part is a valid email or phone number
            when {
                validateEmailUseCase(part) != null -> {
                    InviteViaContactChip(part, true, ClazzInvite.EMAIL)
                }

                phoneNumValidatorUseCase.isValid(part) -> {
                    InviteViaContactChip(part, true, ClazzInvite.PHONE)
                }

                isValidUserName(part) -> {
                    InviteViaContactChip(part, true, ClazzInvite.INTERNAL_MESSAGE)
                }

                else -> {
                    InviteViaContactChip(part, false, 0)
                }
            }
        }
    }


    private fun isValidUserName(username: String): Boolean {
        return username.startsWith("@")
    }
}