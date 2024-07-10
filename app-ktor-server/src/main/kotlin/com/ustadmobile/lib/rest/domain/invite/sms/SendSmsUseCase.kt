package com.ustadmobile.lib.rest.domain.invite.sms

import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class SendSmsUseCase(private val di: DI) {
    suspend operator fun invoke(number: String, link: String) {
        try {
            val sendSmsUseCaseHttp: SendSmsUseCaseHttp = di.direct.instance()

            sendSmsUseCaseHttp.invoke(number, link)
        } catch (e: Exception) {
            Napier.d { "SendSmsUseCase  ${e.message}" }
        }

    }
}