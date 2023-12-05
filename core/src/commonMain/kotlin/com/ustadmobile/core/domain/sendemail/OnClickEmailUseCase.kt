package com.ustadmobile.core.domain.sendemail

interface OnClickEmailUseCase {

    operator fun invoke(emailAddr: String)
}