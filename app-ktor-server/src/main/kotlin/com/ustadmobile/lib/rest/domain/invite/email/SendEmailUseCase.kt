package com.ustadmobile.lib.rest.domain.invite.email

interface SendEmailUseCase {
    operator fun invoke(clazzName: String, email: String, link: String)
}