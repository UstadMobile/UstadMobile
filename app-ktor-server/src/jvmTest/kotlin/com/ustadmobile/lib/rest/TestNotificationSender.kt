package com.ustadmobile.lib.rest

import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

class TestNotificationSender {

    //@Test
    fun givenValidMessage_whenSendEmail_thenShouldReportSuccess() {
        val mailProps = Properties().apply {
            setProperty("mail.smtp.auth", "true")
            setProperty("mail.smtp.starttls.enable", "true")
            setProperty("mail.smtp.host", "smtp.office365.com")
            setProperty("mail.smtp.port", "587")
        }



        val notificationSender = NotificationSender(mailProps, "somewhere@ustadmobile.com",
            "somewhere@ustadmobile.com", "secret")

        runBlocking {
            notificationSender.sendEmail("mike@ustadmobile.com", "Hello Jakarta",
                "Hello Jakarta")
        }

    }

    companion object {



    }

}