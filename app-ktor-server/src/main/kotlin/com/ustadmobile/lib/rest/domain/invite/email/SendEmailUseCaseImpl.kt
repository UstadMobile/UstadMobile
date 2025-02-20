package com.ustadmobile.lib.rest.domain.invite.email

import com.ustadmobile.lib.rest.NotificationSender

class SendEmailUseCaseImpl(
     val notificationSender: NotificationSender
) : SendEmailUseCase {
    override fun invoke(clazzName: String, email: String, link: String) {
        notificationSender.sendEmail(email, "Invitation to $clazzName", link)
    }
}