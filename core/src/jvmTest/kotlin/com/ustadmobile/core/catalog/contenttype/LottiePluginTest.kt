package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.contentjob.LottiePlugin
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class LottiePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var endpointScope: EndpointScope

    private lateinit var activeEndpoint: Endpoint

    @Before
    fun setup() {
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }



        val accountManager: UstadAccountManager by di.instance()
        activeEndpoint = accountManager.activeEndpoint

        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        repo = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_REPO)

    }

    @Test
    fun givenValidLottieFile_whenExtractMetadataCalled_thenShouldGetTitle() {
        print("%%%%%%%%%")
        val inputStream = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/contenttype/lottie.json")
        val tempFile = tmpFolder.newFile()
        tempFile.copyInputStreamToFile(inputStream!!)

        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())

        val lottiePlugin = LottiePlugin(context, activeEndpoint, di)

        val metadata = runBlocking {
            val lottieUri = DoorUri.parse(tempFile.toURI().toString())
            val processContext = ContentJobProcessContext(lottieUri, tempUri, mutableMapOf(),
                DummyContentJobItemTransactionRunner(db), di)
            lottiePlugin.extractMetadata(lottieUri, processContext)
        }

        Assert.assertEquals("Got ContentEntry with expected title",
            "Comp 1",
            metadata.entry.title)

    }

    @Test
    fun givenInvalidFile_whenExtractMetadataCalled_thenShouldReturnNull() {

    }


}