package com.ustadmobile.core.contentformats

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.h5p.H5PContentImporter
import com.ustadmobile.core.contentformats.pdf.PdfContentImporterJvm
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.MimeTypeHelper
import kotlinx.io.files.Path
import nl.adaptivity.xmlutil.serialization.XML
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File

/**
 * ContentImporters module is shared between Desktop and Backend.
 */
val ContentImportersDiModuleJvm = DI.Module("ContentImporters-Jvm"){
    bind<ContentImportersManager>() with scoped(EndpointScope.Default).singleton {
        val cache: UstadCache = instance()
        val uriHelper: UriHelper = instance()
        val xml: XML = instance()
        val xhtmlFixer: XhtmlFixer = instance()
        val db: UmAppDatabase = instance(tag = DoorTag.TAG_DB)
        val saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase = instance()
        val tmpRoot: File = instance(tag = DiTag.TAG_TMP_DIR)
        val contentImportTmpPath = Path(tmpRoot.absolutePath, "contentimport")
        val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase = instance()
        val mimeTypeHelper: MimeTypeHelper = instance()

        ContentImportersManager(
            buildList {
                add(
                    EpubContentImporterCommonJvm(
                        endpoint = context,
                        cache = cache,
                        db = db,
                        uriHelper = uriHelper,
                        xml = xml,
                        xhtmlFixer = xhtmlFixer,
                        tmpPath = contentImportTmpPath,
                        saveLocalUriAsBlobAndManifestUseCase =  saveAndManifestUseCase,
                        json = instance(),
                        getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                        compressListUseCase = instance(),
                        saveLocalUrisAsBlobsUseCase = instance(),
                    )
                )
                add(
                    XapiZipContentImporter(
                        endpoint = context,
                        db = db,
                        cache = cache,
                        uriHelper = uriHelper,
                        json = instance(),
                        tmpPath = contentImportTmpPath,
                        saveLocalUriAsBlobAndManifestUseCase =  saveAndManifestUseCase,
                        compressListUseCase = instance(),
                    )
                )
                add(
                    PdfContentImporterJvm(
                        endpoint = context,
                        db = db,
                        cache= cache,
                        saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                        uriHelper = uriHelper,
                        json = instance(),
                        getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                        compressPdfUseCase = instanceOrNull(),
                        saveLocalUriAsBlobUseCase = instance(),
                        tmpPath = instance(tag = DiTag.TAG_TMP_DIR),
                    ),
                )
                add(
                    H5PContentImporter(
                        endpoint = context,
                        db = db,
                        cache = cache,
                        uriHelper = uriHelper,
                        tmpPath = contentImportTmpPath,
                        saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                        compressListUseCase = instance(),
                        json = instance(),
                    ),
                )

                add(
                    VideoContentImporterCommonJvm(
                        endpoint = context,
                        db = db,
                        cache = cache,
                        uriHelper = uriHelper,
                        validateVideoFileUseCase = instance(),
                        json = instance(),
                        tmpPath = contentImportTmpPath,
                        saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                        getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                        mimeTypeHelper = mimeTypeHelper,
                        compressUseCase = instanceOrNull(),
                        extractVideoThumbnailUseCase = instanceOrNull(),
                        saveLocalUrisAsBlobsUseCase = instanceOrNull(),
                    )
                )
            }
        )
    }

}