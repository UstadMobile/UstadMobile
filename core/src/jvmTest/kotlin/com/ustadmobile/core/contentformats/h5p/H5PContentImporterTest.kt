package com.ustadmobile.core.contentformats.h5p

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.contententry.ContentConstants.MANIFEST_NAME
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.test.assertZipIsManifested
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsString
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.xmlpullparserkmp.setInputString
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import org.xmlpull.v1.XmlPullParserFactory
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class H5PContentImporterTest : AbstractContentImporterTest() {

    @Test
    fun givenValidH5pFile_whenExtractMetadataCalled_thenMetadataShouldMatch() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            compressListUseCase = compressListUseCase,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
        )

        val metadata = runBlocking {
            h5pPlugin.extractMetadata(h5pFile.toDoorUri(), "fill-in-the-blank-withmetadata.h5p")
        }

        assertEquals("I want to eat", metadata?.entry?.title)
        assertEquals("Bob Jones", metadata?.entry?.author)
        assertEquals(ContentEntry.LICENSE_TYPE_CC_BY, metadata?.entry?.licenseType)
    }

    @Test
    fun givenFileWithH5pExtensionNotValidH5p_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val invalidH5pFile = temporaryFolder.newFile()
        invalidH5pFile.writeText("Hello world")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking {
            try {
                h5pPlugin.extractMetadata(invalidH5pFile.toDoorUri(), "invalid.h5p")
                throw IllegalStateException("Should not get here")
            }catch(e: InvalidContentException) {
                assertNotNull(e)
            }
        }
    }

    @Test
    fun givenFileNotH5pFile_whenExtractMetadataCalled_thenShouldReturnNull() {
        val notH5p = temporaryFolder.newFile()

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking {
            assertNull(h5pPlugin.extractMetadata(notH5p.toDoorUri(), "file.pdf"))
        }
    }

    @Test
    fun givenValidH5pFile_whenStoreInCacheCalled_thenShouldStore() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contentformats/fill-in-the-blank-withmetadata.h5p")

        val h5pPlugin = H5PContentImporter(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            json = json,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            compressListUseCase = compressListUseCase,
        )

        val result = runBlocking {
            h5pPlugin.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = h5pFile.toDoorUri().toString(),
                ),
                progressListener =  { }
            )
        }

        val expectedUrlPrefix = "${activeEndpoint.url}api/content/${result.cevUid}/"
        val manifestResponse = ustadCache.retrieve(
            iRequestBuilder("$expectedUrlPrefix$MANIFEST_NAME")
        )
        val manifest = json.decodeFromString(
            ContentManifest.serializer(), manifestResponse!!.bodyAsString()!!
        )

        ZipFile(h5pFile).use { zipFile ->
            ustadCache.assertZipIsManifested(manifest, zipFile, "h5p-folder/")
        }

        val h5pStandaloneAssets = temporaryFolder.newFileFromResource(this::class.java,
            "/h5p/h5p-standalone-3.6.0.zip")

        ZipFile(h5pStandaloneAssets).use { zipFile ->
            ustadCache.assertZipIsManifested(manifest, zipFile)
        }

        //Check we can parse the tincan xml
        val tinCanXmlResponse = ustadCache.retrieve(
            iRequestBuilder(manifest.entries.first { it.uri == "tincan.xml" }.bodyDataUrl)
        )
        val tinCanStr = tinCanXmlResponse?.bodyAsSource()?.asInputStream()?.readString()
        val xppFactory = XmlPullParserFactory.newInstance()
        val xpp = xppFactory.newPullParser()

        xpp.setInputString(tinCanStr!!)
        val tinCanXml = TinCanXML.loadFromXML(xpp)
        assertEquals("index.html", tinCanXml.launchActivity?.launchUrl)

        val htmlResponse = ustadCache.retrieve(
            iRequestBuilder(manifest.entries.first { it.uri == "index.html"}.bodyDataUrl)
        )
        assertNotNull(htmlResponse)
    }

}