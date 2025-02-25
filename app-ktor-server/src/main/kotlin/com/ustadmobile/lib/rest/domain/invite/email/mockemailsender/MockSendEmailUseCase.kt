package com.ustadmobile.lib.rest.domain.invite.email.mockemailsender

import com.ustadmobile.lib.rest.domain.invite.email.SendEmailUseCase
import io.github.aakira.napier.Napier

class MockSendEmailUseCase(
    private val mockEmailSender: MockEmailSender
) : SendEmailUseCase {
    override fun invoke(clazzName: String, email: String, link: String) {
        try {
            mockEmailSender.saveMockEmail(email, "Invitation to $clazzName", link)
        } catch (e: Exception) {
            Napier.e { e.message.toString() }
        }
    }
}