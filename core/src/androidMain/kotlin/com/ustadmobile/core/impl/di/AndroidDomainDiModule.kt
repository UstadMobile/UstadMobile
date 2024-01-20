package com.ustadmobile.core.impl.di

import android.content.Context
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseAndroid
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtilAndroid
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumberUseCaseAndroid
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorAndroid
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCaseAndroid
import com.ustadmobile.core.domain.sms.OnClickSendSmsUseCase
import com.ustadmobile.core.domain.sms.OnClickSendSmsUseCaseAndroid
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

/**
 * Because parts of this module use strings, it is only used by the DI within the activity,
 * not the application.
 */
@Suppress("FunctionName")
fun AndroidDomainDiModule(
    appContext: Context,
): DI.Module {
    return DI.Module("Android-Domain") {

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

        bind<OnClickPhoneNumUseCase>() with provider {
            OnClickPhoneNumberUseCaseAndroid(appContext)
        }

        bind<OnClickSendSmsUseCase>() with provider {
            OnClickSendSmsUseCaseAndroid(appContext)
        }

        bind<OnClickEmailUseCase>() with provider {
            OnClickEmailUseCaseAndroid(appContext)
        }
    }

}