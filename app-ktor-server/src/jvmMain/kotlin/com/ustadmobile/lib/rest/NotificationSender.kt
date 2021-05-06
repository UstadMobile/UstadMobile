package com.ustadmobile.lib.rest

import com.ustadmobile.lib.rest.ext.toProperties
import io.ktor.config.*
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class NotificationSender(private val mailProps: Properties,
                         private val fromAddress: String,
                         private val username: String, private val password: String) {

    constructor(appConfig: ApplicationConfig): this(appConfig.toProperties(SMTP_PROPS),
                appConfig.property("mail.from").getString(),
                appConfig.property("mail.user").getString(),
                appConfig.property("mail.auth").getString())

    val authenticator: Authenticator = object: Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    suspend fun sendEmail(toAddr: String, subject: String, message: String) = withContext(Dispatchers.IO) {
        val session = Session.getInstance(mailProps, authenticator)

        val mimeMessage = MimeMessage(session).also {
            it.setFrom(InternetAddress(fromAddress))
            it.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddr))
            it.subject = subject
            it.setContent(MimeMultipart().apply {
                addBodyPart(MimeBodyPart().apply {
                    setText(message)
                })
            })
        }

        Transport.send(mimeMessage)
    }

    companion object {
        val SMTP_PROPS = listOf("mail.smtp.auth", "mail.smtp.starttls.enable", "mail.smtp.host",
            "mail.smtp.port")
    }

}