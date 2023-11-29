package com.ustadmobile.core.domain.phonenumber

import web.window.WindowTarget
import web.window.window

class OnClickPhoneNumUseCaseJs: OnClickPhoneNumUseCase {

    override fun invoke(number: String) {
        window.open("tel:$number", WindowTarget._blank)
    }

}