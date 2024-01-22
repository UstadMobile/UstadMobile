package com.ustadmobile.core.domain.phonenumber

import java.awt.Desktop
import java.net.URI

class OnClickPhoneNumUseCaseJvm : OnClickPhoneNumUseCase{

    override fun invoke(number: String) {
        try {
            Desktop.getDesktop().browse(URI("tel:$number"))
        }catch(e: Throwable) {
            e.printStackTrace()
        }
    }
}