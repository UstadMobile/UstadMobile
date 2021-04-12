package com.ustadmobile.port.sharedse.contentformats

import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImpl
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import com.ustadmobile.sharedse.util.UstadTestRule
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.nio.file.Files

class ContentTypePluginsTest {

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var contentImportManager: ContentImportManager

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    private lateinit var httpClient: HttpClient

    @Before
    fun setup(){

        di = DI {
            import(ustadTestRule.diModule)
            bind<ContentImportManager>() with scoped(ustadTestRule.endpointScope!!).singleton {
                ContentImportManagerImpl(listOf(EpubTypePluginCommonJvm()), context, this.context, di)
            }
        }

        httpClient = di.direct.instance()

        val accountManager: UstadAccountManager by di.instance()
        contentImportManager =  di.on(accountManager.activeAccount).direct.instance()

    }


    @Test
    fun givenValidEpubFormatFile_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = temporaryFolder.newFile("imported.epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = temporaryFolder.newFolder("containerTmpDir")

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        val dbRepo = db.asRepository<UmAppDatabase>(context, "http://localhost/dummy", "",
            httpClient, containerTmpDir.absolutePath)

        runBlocking {
            //TODO: Make this more rigorous
            val metadata = contentImportManager.extractMetadata(tempEpubFile.path)!!
            val container = contentImportManager.importFileToContainer(tempEpubFile.path,
                    metadata.mimeType, 0, containerTmpDir.absolutePath, mapOf()){

            }
            Assert.assertNotNull(metadata.contentEntry)
            Assert.assertNotNull(container)
        }

        containerTmpDir.deleteRecursively()
        tempEpubFile.delete()
    }


    @Test
    fun givenUnsupportedFileFormat_whenImported_shouldReturnNull(){
        val emptyFile = temporaryFolder.newFile("empty.zip")

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()

        val contentEntry =  runBlocking {
           contentImportManager.extractMetadata(emptyFile.path)?.contentEntry
        }

        Assert.assertNull("Given unsupported file, extractContentEntryMetaData returns null",
            contentEntry)
    }
}