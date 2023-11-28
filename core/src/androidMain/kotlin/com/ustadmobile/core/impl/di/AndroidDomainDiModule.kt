package com.ustadmobile.core.impl.di

import android.content.Context
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCaseAndroid
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseAndroid
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtilAndroid
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorAndroid
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.door.ext.DoorTag
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

fun AndroidDomainDiModule(
    appContext: Context,
    endpointScope: EndpointScope
): DI.Module {
    return DI.Module("Android-Domain") {
        bind<ImportContentUseCase>() with scoped(endpointScope).singleton {
            val db: UmAppDatabase = on(context).instance(tag = DoorTag.TAG_DB)
            ImportContentUseCaseAndroid(context, appContext, db)
        }

        bind<OpenExternalLinkUseCase>() with provider {
            OpenExternalLinkUseCaseAndroid(appContext)
        }

        bind<IPhoneNumberUtil>() with provider {
            IPhoneNumberUtilAndroid(PhoneNumberUtil.createInstance(appContext))
        }

        bind<PhoneNumValidatorUseCase>() with provider {
            PhoneNumValidatorAndroid(
                iPhoneNumberUtil = instance()
            )
        }
    }

}