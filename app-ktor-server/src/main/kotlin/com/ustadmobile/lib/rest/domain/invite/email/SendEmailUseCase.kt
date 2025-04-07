package com.ustadmobile.lib.rest.domain.invite.email

interface SendEmailUseCase {
    operator fun invoke(subject: String, email: String, link: String)
}