package com.ustadmobile.core.domain.sendemail

import java.awt.Desktop
import java.net.URI

class OnClickEmailUseCaseJvm: OnClickEmailUseCase {

    override fun invoke(emailAddr: String) {
        try {
            Desktop.getDesktop().mail(URI("mailto:$emailAddr"))
        }catch(e: Exception) {
            //do nothing
        }
    }
}