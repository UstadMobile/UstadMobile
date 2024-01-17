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
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCaseCommonJvm
import com.ustadmobile.core.domain.blob.download.ContentManifestDownloadUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.download.EnqueueContentManifestDownloadUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueContentManifestDownloadUseCaseJvm
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJvm
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCaseJvm
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCaseJvm
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
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

    bind<ChunkedUploadClientUseCaseKtorImpl>() with singleton {
        ChunkedUploadClientUseCaseKtorImpl(
            httpClient = instance(),
            uriHelper = instance(),
        )
    }

    bind<ChunkedUploadClientLocalUriUseCase>() with singleton {
        instance<ChunkedUploadClientUseCaseKtorImpl>()
    }

    bind<ChunkedUploadClientChunkGetterUseCase>() with singleton {
        instance<ChunkedUploadClientUseCaseKtorImpl>()
    }

    bind<SaveLocalUrisAsBlobsUseCase>() with scoped(EndpointScope.Default).singleton {
        val tmpDir = instance<File>(tag = DiTag.TAG_TMP_DIR)

        SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = context,
            cache = instance(),
            uriHelper = instance(),
            tmpDir = Path(tmpDir.absolutePath),
            fileSystem = SystemFileSystem,
            deleteUrisUseCase = instance(),
            createRetentionLock = true,
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
            deleteUrisUseCase = instance()
        )
    }

    bind<IsTempFileCheckerUseCase>() with singleton {
        IsTempFileCheckerUseCaseJvm(
            tmpRootDir = instance<File>(tag = DiTag.TAG_TMP_DIR)
        )
    }

    bind<DeleteUrisUseCase>() with singleton {
        DeleteUrisUseCaseCommonJvm(
            isTempFileCheckerUseCase = instance()
        )
    }

    bind<UpdateFailedTransferJobUseCase>() with scoped(EndpointScope.Default).singleton {
        UpdateFailedTransferJobUseCase(
            db = instance(tag = DoorTag.TAG_DB)
        )
    }

    bind<ContentEntryGetMetaDataFromUriUseCase>() with scoped(EndpointScope.Default).singleton {
        ContentEntryGetMetaDataFromUriUseCaseCommonJvm(
            importersManager = instance()
        )
    }

    bind<SaveLocalUriAsBlobAndManifestUseCase>() with scoped(EndpointScope.Default).singleton {
        SaveLocalUriAsBlobAndManifestUseCaseJvm(
            saveLocalUrisAsBlobsUseCase = instance()
        )
    }

    bind<ImportContentEntryUseCase>() with scoped(EndpointScope.Default).singleton {
        ImportContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            importersManager = instance(),
            enqueueBlobUploadClientUseCase = instance(),
            createRetentionLocksForManifestUseCase = instance(),
            httpClient = instance()
        )
    }

    bind<CreateRetentionLocksForManifestUseCase>() with scoped(EndpointScope.Default).singleton {
        CreateRetentionLocksForManifestUseCaseCommonJvm(
            cache = instance()
        )
    }

    bind<BlobDownloadClientUseCase>() with scoped(EndpointScope.Default).singleton {
        BlobDownloadClientUseCaseCommonJvm(
            okHttpClient = instance(),
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO)
        )
    }

    bind<EnqueueBlobDownloadClientUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueBlobDownloadClientUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB)
        )
    }

    bind<ContentManifestDownloadUseCase>() with scoped(EndpointScope.Default).singleton {
        ContentManifestDownloadUseCase(
            enqueueBlobDownloadClientUseCase = instance(),
            db = instance(tag = DoorTag.TAG_DB),
            httpClient = instance(),
        )
    }

    bind<EnqueueContentManifestDownloadUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueContentManifestDownloadUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB),
        )
    }


}