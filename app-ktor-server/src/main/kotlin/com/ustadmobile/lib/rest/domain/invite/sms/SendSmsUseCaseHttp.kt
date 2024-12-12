package com.ustadmobile.lib.rest.domain.invite.sms

import com.ustadmobile.lib.rest.domain.invite.sms.twilio.TwilioHttpClient
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import org.kodein.di.direct
import org.kodein.di.instance

class SendSmsUseCaseHttp(
    private val di: DI,
) {
    suspend operator fun invoke(
        toNumber: String,
        link: String,
    ) {
        try {
            val smsProperties: SmsProperties = di.direct.instance()
            val client: TwilioHttpClient = di.direct.instance()
            val response = client.invoke().submitForm(
                url = smsProperties.providerLink,
                formParameters = Parameters.build {
                    append("To", toNumber)
                    append("From", smsProperties.fromPhone)
                    append(
                        "Body",
                        "Here is a link to join the class course ${link} "
                    )
                }
            )

            Napier.d { "SMS result: ${response}" }

        } catch (e: Exception) {
            Napier.d { "Failed to send SMS: ${e.message}" }

        }

    }

}








