package com.ustadmobile.lib.rest.domain.invite.email

import com.ustadmobile.lib.rest.NotificationSender

class SendEmailUseCase(val notificationSender: NotificationSender) {
    operator fun invoke(email: String, link: String) {
        notificationSender.sendEmail(email, "", link)
    }
}