package com.ustadmobile.lib.rest.domain.invite.email.mockemailsender

import kotlinx.serialization.Serializable

class MockEmailSender {
    private val sentEmails = mutableListOf<MockEmail>()

    fun saveMockEmail(
        sentTo: String,
        subject: String,
        text: String
    ){
        sentEmails.add(MockEmail(sentTo, subject, text))
    }

    fun getEmailsForUser(sentTo: String): List<MockEmail> {
        return sentEmails.filter { it.sentTo == sentTo }
    }

}
@Serializable
data class MockEmail(
    val sentTo: String,
    val subject: String,
    val text: String
)
