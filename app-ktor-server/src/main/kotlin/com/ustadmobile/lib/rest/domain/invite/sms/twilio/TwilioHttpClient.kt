package com.ustadmobile.lib.rest.domain.invite.sms.twilio

import com.ustadmobile.lib.rest.domain.invite.sms.SmsProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class TwilioHttpClient(
    di: DI
) {
    private val smsProperties: SmsProperties = di.direct.instance()

    operator fun invoke(): HttpClient {
        val client = HttpClient(OkHttp) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(

                            username = smsProperties.sid,
                            password = smsProperties.token
                        )
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
            }

        }
        return client
    }
}