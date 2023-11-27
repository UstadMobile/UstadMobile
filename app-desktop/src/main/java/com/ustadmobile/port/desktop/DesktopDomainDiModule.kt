package com.ustadmobile.port.desktop

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJvm
import com.ustadmobile.core.domain.phonenumvalidator.PhoneNumValidatorJvm
import com.ustadmobile.core.domain.phonenumvalidator.PhoneNumValidatorUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider

val DesktopDomainDiModule = DI.Module("Desktop-Domain") {
    bind<OpenExternalLinkUseCase>() with provider {
        OpenExternalLinkUseCaseJvm()
    }

    bind<PhoneNumValidatorUseCase>() with provider {
        PhoneNumValidatorJvm()
    }

}