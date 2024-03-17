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
import com.ustadmobile.core.domain.getversion.GetVersionUseCaseJvm
import com.ustadmobile.core.domain.account.SetPasswordUseCase
import com.ustadmobile.core.domain.account.SetPasswordUseCaseCommonJvm
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.BlobDownloadClientUseCaseCommonJvm
import com.ustadmobile.core.domain.blob.download.CancelDownloadUseCase
import com.ustadmobile.core.domain.blob.download.CancelDownloadUseCaseJvm
import com.ustadmobile.core.domain.blob.download.ContentManifestDownloadUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueBlobDownloadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.download.EnqueueContentManifestDownloadUseCase
import com.ustadmobile.core.domain.blob.download.EnqueueContentManifestDownloadUseCaseJvm
import com.ustadmobile.core.domain.blob.download.MakeContentEntryAvailableOfflineUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUiUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCaseJvm
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.saveandupload.SaveAndUploadLocalUrisUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseJvm
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.CancelBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.CancelBlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.EnqueueBlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.UpdateFailedTransferJobUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCaseJvm
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJvm
import com.ustadmobile.core.domain.contententry.delete.DeleteContentEntryParentChildJoinUseCase
import com.ustadmobile.core.domain.contententry.getlocalurlforcontent.GetLocalUrlForContentUseCase
import com.ustadmobile.core.domain.contententry.getlocalurlforcontent.GetLocalUrlForContentUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CreateRetentionLocksForManifestUseCaseCommonJvm
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.epub.LaunchEpubUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.epub.LaunchEpubUseCaseJvm
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.LaunchChromeUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJvm
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCaseJvm
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.ResolveXapiLaunchHrefUseCase
import com.ustadmobile.core.domain.contententry.move.MoveContentEntriesUseCase
import com.ustadmobile.core.domain.deleteditem.DeletePermanentlyUseCase
import com.ustadmobile.core.domain.deleteditem.RestoreDeletedItemUseCase
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.launchopenlicenses.LaunchOpenLicensesUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsFromLocalUriUseCaseCommonJvm
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCase
import com.ustadmobile.core.domain.person.bulkadd.BulkAddPersonsUseCaseImpl
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCaseJvm
import com.ustadmobile.core.domain.process.CloseProcessUseCase
import com.ustadmobile.core.domain.process.CloseProcessUseCaseJvm
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCaseJvm
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.domain.validateemail.ValidateEmailUseCase
import com.ustadmobile.core.impl.config.AppConfig
import com.ustadmobile.core.impl.config.AppConfig.Companion.KEY_CONFIG_SHOW_POWERED_BY
import com.ustadmobile.core.launchopenlicenses.LaunchOpenLicensesUseCaseJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.requireFileSeparatorSuffix
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
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
            json = instance(),
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
            saveLocalUrisAsBlobsUseCase = instance(),
            mimeTypeHelper = FileMimeTypeHelperImpl(),
        )
    }

    bind<ImportContentEntryUseCase>() with scoped(EndpointScope.Default).singleton {
        ImportContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            importersManager = instance(),
            enqueueBlobUploadClientUseCase = instance(),
            createRetentionLocksForManifestUseCase = instance(),
            httpClient = instance(),
            json = instance(),
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
            repo = instance(tag = DoorTag.TAG_REPO),
            httpCache = instance(),
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
            json = instance(),
            cacheTmpPath = instance<File>(tag = TAG_CACHE_DIR).absolutePath.requireFileSeparatorSuffix()
        )
    }

    bind<EnqueueContentManifestDownloadUseCase>() with scoped(EndpointScope.Default).singleton {
        EnqueueContentManifestDownloadUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB),
        )
    }

    bind<SetPasswordUseCase>() with scoped(EndpointScope.Default).singleton {
        SetPasswordUseCaseCommonJvm(authManager = instance())
    }



    bind<GetStoragePathForUrlUseCase>() with singleton {
        GetStoragePathForUrlUseCaseCommonJvm(
            okHttpClient = instance(),
            cache = instance(),
            tmpDir = instance(tag = DiTag.TAG_TMP_DIR)
        )
    }

    bind<ResolveXapiLaunchHrefUseCase>() with scoped(EndpointScope.Default).singleton {
        ResolveXapiLaunchHrefUseCase(
            activeRepo = instance(tag = DoorTag.TAG_REPO),
            httpClient = instance(),
            json = instance(),
            xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
        )
    }

    bind<LaunchChromeUseCase>() with singleton {
        LaunchChromeUseCase(workingDir = ustadAppDataDir())
    }

    bind<LaunchXapiUseCase>() with scoped(EndpointScope.Default).singleton {
        LaunchXapiUseCaseJvm(
            endpoint = context,
            resolveXapiLaunchHrefUseCase = instance(),
            embeddedHttpServer = instance(),
            launchChromeUseCase = instance(),
        )
    }

    bind<LaunchEpubUseCase>() with scoped(EndpointScope.Default).singleton {
        LaunchEpubUseCaseJvm(
            launchChromeUseCase = instance(),
            embeddedHttpServer = instance(),
            endpoint = context,
            systemImpl = instance(),
        )
    }

    bind<ContentEntryVersionServerUseCase>() with scoped(EndpointScope.Default).singleton {
        ContentEntryVersionServerUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
            okHttpClient = instance(),
            json = instance(),
            onlyIfCached = false,
        )
    }

    bind<GetLocalUrlForContentUseCase>() with scoped(EndpointScope.Default).provider {
        GetLocalUrlForContentUseCaseCommonJvm(
            endpoint = context,
            embeddedHttpServer = instance(),
        )
    }

    bind<GetVersionUseCase>() with singleton {
        GetVersionUseCaseJvm()
    }

    bind<LaunchOpenLicensesUseCase>() with singleton {
        LaunchOpenLicensesUseCaseJvm(
            launchChromeUseCase = instance(),
            licenseFile = File(ustadAppResourcesDir(), "open_source_licenses.html")
        )
    }

    bind<GetShowPoweredByUseCase>() with provider {
        GetShowPoweredByUseCase(
            instance<AppConfig>()[KEY_CONFIG_SHOW_POWERED_BY]?.toBoolean() ?: false
        )
    }

    bind<SetClipboardStringUseCase>() with provider {
        SetClipboardStringUseCaseJvm()
    }

    bind<MoveContentEntriesUseCase>() with scoped(EndpointScope.Default).provider {
        MoveContentEntriesUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            systemImpl = instance()
        )
    }

    bind<DeleteContentEntryParentChildJoinUseCase>() with scoped(EndpointScope.Default).provider {
        DeleteContentEntryParentChildJoinUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<RestoreDeletedItemUseCase>() with scoped(EndpointScope.Default).provider {
        RestoreDeletedItemUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<DeletePermanentlyUseCase>() with scoped(EndpointScope.Default).provider {
        DeletePermanentlyUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<MakeContentEntryAvailableOfflineUseCase>() with scoped(EndpointScope.Default).singleton {
        MakeContentEntryAvailableOfflineUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            nodeIdAndAuth = instance(),
            enqueueContentManifestDownloadUseCase = instance(),
        )
    }

    bind<CancelDownloadUseCase>() with scoped(EndpointScope.Default).singleton {
        CancelDownloadUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB),
        )
    }

    bind<CloseProcessUseCase>() with scoped(EndpointScope.Default).provider {
        CloseProcessUseCaseJvm()
    }

    bind<SaveAndUploadLocalUrisUseCase>() with scoped(EndpointScope.Default).singleton {
        SaveAndUploadLocalUrisUseCase(
            saveLocalUrisAsBlobsUseCase = instance(),
            enqueueBlobUploadClientUseCase = instance(),
            activeDb = instance(tag = DoorTag.TAG_DB),
            activeRepo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<CancelBlobUploadClientUseCase>() with scoped(EndpointScope.Default).singleton {
        CancelBlobUploadClientUseCaseJvm(
            scheduler = instance(),
            endpoint = context,
            db = instance(tag = DoorTag.TAG_DB),
        )
    }

    bind<OpenBlobUseCase>() with scoped(EndpointScope.Default).singleton {
        OpenBlobUseCaseJvm(
            getStoragePathForUrlUseCase = instance(),
            rootTmpDir = instance(tag = DiTag.TAG_TMP_DIR)
        )
    }

    bind<OpenBlobUiUseCase>() with scoped(EndpointScope.Default).singleton {
        OpenBlobUiUseCase(
            openBlobUseCase = instance(),
            systemImpl = instance(),
        )
    }

    bind<BulkAddPersonsUseCase>() with scoped(EndpointScope.Default).singleton {
        BulkAddPersonsUseCaseImpl(
            addNewPersonUseCase = instance(),
            validateEmailUseCase = instance(),
            validatePhoneNumUseCase = instance(),
            authManager = instance(),
            enrolUseCase = instance(),
            activeDb = instance(tag = DoorTag.TAG_DB),
            activeRepo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<BulkAddPersonsFromLocalUriUseCase>() with scoped(EndpointScope.Default).singleton {
        BulkAddPersonsFromLocalUriUseCaseCommonJvm(
            bulkAddPersonsUseCase = instance(),
            uriHelper = instance(),
        )
    }

    bind<ValidateEmailUseCase>() with provider {
        ValidateEmailUseCase()
    }



}