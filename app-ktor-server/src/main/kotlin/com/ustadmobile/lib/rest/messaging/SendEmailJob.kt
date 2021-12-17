package com.ustadmobile.lib.rest.messaging

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.quartz.Job
import org.quartz.JobExecutionContext

class SendEmailJob : Job{

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap

        val mailAuthenticator: Authenticator = di.direct.instance()
        val mailProps: MailProperties = di.direct.instance()

        val session = Session.getInstance(mailProps.props, mailAuthenticator)

        val mimeMessage = MimeMessage(session).also {
            it.setFrom(InternetAddress(mailProps.fromAddr))
            it.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(jobDataMap.getString(INPUT_TO)))
            it.subject = jobDataMap.getString(INPUT_SUBJECT)
            it.setContent(MimeMultipart().apply {
                addBodyPart(MimeBodyPart().apply {
                    setText(jobDataMap.getString(INPUT_MESSAGE))
                })
            })
        }

        Transport.send(mimeMessage)
    }

    companion object {

        const val INPUT_TO = "to"

        const val INPUT_SUBJECT = "subject"

        const val INPUT_MESSAGE = "message"

    }
}