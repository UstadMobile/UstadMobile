package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImpl
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import kotlin.random.Random
import com.ustadmobile.door.entities.NodeIdAndAuth

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

    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup(){

        di = DI {
            import(ustadTestRule.diModule)
            bind<ContentImportManager>() with scoped(ustadTestRule.endpointScope!!).singleton {
                ContentImportManagerImpl(listOf(EpubTypePluginCommonJvm()),
                        context, this.context, di)
            }
        }

        okHttpClient = di.direct.instance()
        httpClient = di.direct.instance()

        val accountManager: UstadAccountManager by di.instance()
        contentImportManager =  di.on(accountManager.activeAccount).direct.instance()

    }


    @Test
    fun givenValidEpubFormatFile_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")
        val tempEpubFile = temporaryFolder.newFile("imported.epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = temporaryFolder.newFolder("containerTmpDir")

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(), randomUuid().toString())
        val db = UmAppDatabase.getInstance(context, nodeIdAndAuth)
        db.clearAllTables()
        val dbRepo = db.asRepository(repositoryConfig(context, "http://localhost/dummy",
            nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient
        ){
            attachmentsDir = containerTmpDir.absolutePath
        })

        runBlocking {
            //TODO: Make this more rigorous
            val metadata = contentImportManager.extractMetadata(tempEpubFile.toURI().toString())!!
            val container = contentImportManager.importFileToContainer(tempEpubFile.toURI().toString(),
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

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(), randomUuid().toString())
        val db = UmAppDatabase.getInstance(context, nodeIdAndAuth)
        db.clearAllTables()

        val contentEntry =  runBlocking {
           contentImportManager.extractMetadata(emptyFile.toURI().toString())?.contentEntry
        }

        Assert.assertNull("Given unsupported file, extractContentEntryMetaData returns null",
            contentEntry)
    }
}