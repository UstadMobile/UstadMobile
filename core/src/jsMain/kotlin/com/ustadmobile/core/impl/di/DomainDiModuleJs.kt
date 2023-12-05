package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.import.ImportContentUseCaseJs
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJs
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJs
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCaseJs
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCaseJs
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumberUtilJs
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickSendEmailUseCaseJs
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

    bind<IPhoneNumberUtil>() with singleton {
        PhoneNumberUtilJs()
    }

    bind<PhoneNumValidatorUseCase>() with provider {
        PhoneNumValidatorUseCaseJs()
    }

    bind<OnClickPhoneNumUseCase>() with provider {
        OnClickPhoneNumUseCaseJs()
    }

    bind<OnClickEmailUseCase>() with provider {
        OnClickSendEmailUseCaseJs()
    }

    bind<SetLanguageUseCase>() with provider {
        SetLanguageUseCaseJs(
            systemImpl = instance()
        )
    }
}
