package com.ustadmobile.core.contentformats.epub

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.ContentDispatcher
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.test.assertCachedBodyMatchesZipEntry
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsUncompressedSourceIfContentEncoded
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.readString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.zip.ZipFile
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EpubContentImporterCommonJvmTest : AbstractContentImporterTest() {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var xml: XML

    private lateinit var xhtmlFixer: XhtmlFixer

    @OptIn(ExperimentalXmlUtilApi::class)
    @Before
    fun epubSetup(){
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()

        xml = XML {
            defaultPolicy {
                unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }
        xhtmlFixer = XhtmlFixerJsoup(xml)
    }

    @AfterTest
    fun epubTearDown() {
        mockWebServer.shutdown()
    }



    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")!!
        val tempEpubFile = temporaryFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            compressListUseCase = compressListUseCase,
            saveLocalUrisAsBlobsUseCase = saveLocalUrisUseCase,
        )

        runBlocking {
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())
            val metadata = epubPlugin.extractMetadata(
                epubUri, "childrens-literature.epub"
            )
            Assert.assertEquals("Got ContentEntry with expected title",
                    "Children's Literature",
                    metadata!!.entry.title)

            //After extracting metadata, the cache should have the cover image
            val coverImgUrl = metadata.picture?.cepPictureUri
            assertNotNull(coverImgUrl)
            assertNotNull(ustadCache.getCacheEntry(coverImgUrl))
        }
    }

    @Test
    fun givenEpubWithoutOpf_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val tmpEpubFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-no-opf.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-no-opf.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("epub does not contain opf") == true
                )
            }
        }
    }

    @Test
    fun givenEpubWithoutNav_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        val tmpEpubFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-no-nav.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-no-nav.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("EPUB/nav.xhtml not found for") == true
                )
            }
        }
    }

    @Test
    fun givenEpubWithManifestItemsMissing_whenExtractMetadataCalled_thenShouldThrowInvalidContentException() {
        //Resource is missing epub.css
        val tmpEpubFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/epub-with-missing-item.epub")

        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking {
            try {
                epubPlugin.extractMetadata(tmpEpubFile.toDoorUri(), "epub-with-missing-item.epub")
                throw IllegalStateException("shouldnt get here")
            }catch(e: InvalidContentException) {
                assertTrue(
                    e.message?.contains("Item(s) from manifest are missing") == true &&
                    e.message?.contains("epub.css") == true
                )
            }
        }
    }

    @Test
    fun givenValidEpubLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){
        val epubPlugin = EpubContentImporterCommonJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            uriHelper = uriHelper,
            xml = xml,
            xhtmlFixer = xhtmlFixer,
            tmpPath = Path(rootTmpFolder.absolutePath),
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            compressListUseCase = compressListUseCase,
        )

        runBlocking{
            val doorUri = DoorUri.parse(
                mockWebServer.url("/com/ustadmobile/core/contenttype/childrens-literature.epub")
                    .toString()
            )

            val uid = db.contentEntryDao().insert(ContentEntry().apply{
                title = "hello"
            })
            val contentEntryChildUid = 42L

            
            val jobItem = ContentEntryImportJob(
                sourceUri = doorUri.uri.toString(),
                cjiParentContentEntryUid = uid,
                cjiContentEntryUid = contentEntryChildUid
            )

            val result = epubPlugin.importContent(
                jobItem = jobItem,
                progressListener =  { }
            )
            assertEquals(contentEntryChildUid, result.cevContentEntryUid)

            withContext(Dispatchers.IO) {
                val tempEpubFile = temporaryFolder.newFileFromResource(this::class.java,
                    "/com/ustadmobile/core/contenttype/childrens-literature.epub")


                val expectedUrlPrefix = "${activeEndpoint.url}api/content/${result.cevUid}/"
                val manifestResponse = ustadCache.retrieve(
                    iRequestBuilder("$expectedUrlPrefix${ContentConstants.MANIFEST_NAME}")
                )
                val manifest = json.decodeFromString(
                    ContentManifest.serializer(), manifestResponse!!.bodyAsUncompressedSourceIfContentEncoded()!!.readString()
                )


                ZipFile(tempEpubFile).use { zipFile ->
                    //verifies that we have the OPF
                    val opfPathInZip = result.cevOpenUri!!.substringAfterLast("api/content/${result.cevUid}/")
                    val manifestOpfEntry = manifest.entries.first {
                        it.uri == opfPathInZip
                    }


                    ustadCache.assertCachedBodyMatchesZipEntry(
                        url = manifestOpfEntry.bodyDataUrl,
                        zipFile = zipFile,
                        pathInZip = opfPathInZip
                    )

                    val opfResponse = ustadCache.retrieve(iRequestBuilder(manifestOpfEntry.bodyDataUrl))
                    val opfPackage = xml.decodeFromString(
                        deserializer = PackageDocument.serializer(),
                        string = opfResponse?.bodyAsSource()!!.readString()
                    )

                    //Assert that we have all content in the manifest with the expected mimetype
                    opfPackage.manifest.items.forEach { opfItem ->
                        val pathInZipFile = Path(opfPathInZip).parent?.let {
                            Path(it, opfItem.href)
                        } ?: Path(opfItem.href)
                        val pathInZipFileStr = pathInZipFile.toString()

                        val manifestItem = manifest.entries.first {
                            it.uri == pathInZipFileStr
                        }

                        assertEquals(opfItem.mediaType, manifestItem.responseHeaders["content-type"])

                        ustadCache.assertCachedBodyMatchesZipEntry(
                            url = manifestItem.bodyDataUrl,
                            zipFile = zipFile,
                            pathInZip = pathInZipFile.toString()
                        )
                    }
                }
            }
        }
    }

}