package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.import.ImportContentUseCaseJs
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJs
import com.ustadmobile.core.domain.phonenumvalidator.PhoneNumValidatorJs
import com.ustadmobile.core.domain.phonenumvalidator.PhoneNumValidatorUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

fun DomainDiModuleJs(endpointScope: EndpointScope) = DI.Module("DomainDiModuleJs") {
    bind<ImportContentUseCase>() with scoped(endpointScope).provider {
        ImportContentUseCaseJs(
            endpoint = context,
            httpClient = instance(),
        )
    }

    bind<OpenExternalLinkUseCase>() with provider {
        OpenExternalLinkUseCaseJs()
    }

    bind<OnClickLinkUseCase>() with singleton {
        OnClickLinkUseCase(
            navController = instance(),
            accountManager = instance(),
            openExternalLinkUseCase = instance(),
            apiUrlConfig = instance(),
        )
    }

    bind<PhoneNumValidatorUseCase>() with provider {
        PhoneNumValidatorJs()
    }
}
