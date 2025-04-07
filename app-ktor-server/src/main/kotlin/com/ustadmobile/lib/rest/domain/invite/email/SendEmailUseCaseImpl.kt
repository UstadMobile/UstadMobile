package com.ustadmobile.lib.rest.domain.invite.email

import com.ustadmobile.lib.rest.NotificationSender

class SendEmailUseCaseImpl(
     val notificationSender: NotificationSender
) : SendEmailUseCase {
    override fun invoke(subject: String, email: String, link: String) {
        notificationSender.sendEmail(email, subject, link)
    }
}