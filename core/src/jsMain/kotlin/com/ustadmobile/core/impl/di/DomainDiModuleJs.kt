package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobUseCaseJs
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseJs
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJs
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
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCaseJs
import com.ustadmobile.door.ext.DoorTag
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
            languagesConfig = instance()
        )
    }

    bind<ChunkedUploadClientLocalUriUseCase>() with singleton {
        ChunkedUploadClientLocalUriUseCaseJs()
    }

    bind<EnqueueSavePictureUseCase>() with scoped(endpointScope).singleton {
        EnqueueSavePictureUseCaseJs(
            savePictureUseCase = instance()
        )
    }

    bind<SavePictureUseCase>() with scoped(endpointScope).singleton {
        SavePictureUseCase(
            saveLocalUrisAsBlobUseCase = instance(),
            enqueueBlobUploadClientUseCase = null,
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
            compressImageUseCase = CompressImageUseCaseJs(),
        )
    }

    bind<SaveLocalUrisAsBlobsUseCase>() with scoped(endpointScope).singleton {
        SaveLocalUrisAsBlobUseCaseJs(
            chunkedUploadClientLocalUriUseCase = instance(),
            endpoint = context,
            json = instance(),
            db = instance(tag = DoorTag.TAG_DB),
        )
    }

}
