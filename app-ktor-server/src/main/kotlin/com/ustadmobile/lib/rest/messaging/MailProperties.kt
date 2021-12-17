package com.ustadmobile.lib.rest.messaging

import java.util.*

data class MailProperties(val fromAddr: String, val props: Properties) {
    companion object {
        val SMTP_PROPS = listOf("mail.smtp.auth", "mail.smtp.starttls.enable", "mail.smtp.host",
            "mail.smtp.port")
    }
}
