package com.ustadmobile.port.desktop

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJvm
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorJvm
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumberUtilJvm
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.provider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJvm
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCaseJvm
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCaseJvm
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File

val DesktopDomainDiModule = DI.Module("Desktop-Domain") {
    bind<OpenExternalLinkUseCase>() with provider {
        OpenExternalLinkUseCaseJvm()
    }

    bind<IPhoneNumberUtil>() with provider {
        PhoneNumberUtilJvm(PhoneNumberUtil.getInstance())
    }

    bind<PhoneNumValidatorUseCase>() with provider {
        PhoneNumValidatorJvm(
            iPhoneNumberUtil = instance()
        )
    }

    bind<OnClickPhoneNumUseCase>() with provider {
        OnClickPhoneNumUseCaseJvm()
    }

    bind<SetLanguageUseCase>() with provider {
        SetLanguageUseCaseJvm(
            supportedLangConfig = instance()
        )
    }

    bind<OnClickEmailUseCase>() with provider {
        OnClickEmailUseCaseJvm()
    }

    bind<ChunkedUploadClientUseCase>() with singleton {
        ChunkedUploadClientUseCase(
            httpClient = instance()
        )
    }

    bind<SaveLocalUrisAsBlobsUseCase>() with scoped(EndpointScope.Default).singleton {
        val tmpDir = instance<File>(tag = DiTag.TAG_TMP_DIR)

        SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = context,
            cache = instance(),
            uriHelper = instance(),
            tmpDir = Path(tmpDir.absolutePath),
            fileSystem = SystemFileSystem
        )
    }

    bind<EnqueueBlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueBlobUploadClientUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB),
            cache = instance()
        )
    }

    bind<BlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
        BlobUploadClientUseCaseJvm(
            chunkedUploadUseCase = on(context).instance(),
            httpClient = instance(),
            httpCache = instance(),
            db = on(context).instance(tag = DoorTag.TAG_DB),
            repo = on(context).instance(tag = DoorTag.TAG_REPO),
            endpoint = context,
        )
    }

    bind<EnqueueSavePictureUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueSavePictureUseCaseJvm(
            scheduler = instance(),
            endpoint = context
        )
    }

    bind<SavePictureUseCase>() with scoped(EndpointScope.Default).singleton {
        SavePictureUseCase(
            saveLocalUrisAsBlobUseCase = on(context).instance(),
            db = on(context).instance(tag = DoorTag.TAG_DB),
            repo = on(context).instance(tag = DoorTag.TAG_REPO),
            enqueueBlobUploadClientUseCase = on(context).instance(),
            compressImageUseCase = CompressImageUseCaseJvm(),
        )
    }







}