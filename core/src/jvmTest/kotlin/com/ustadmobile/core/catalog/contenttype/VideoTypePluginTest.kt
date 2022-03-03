package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.onActiveAccountDirect
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.instance

class VideoTypePluginTest {


    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    private lateinit var db: UmAppDatabase

    @Before
    fun setup(){

        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()

        db = di.onActiveAccountDirect().instance(tag = DoorTag.TAG_DB)
    }

    @Test
    fun givenValidVideo_whenExtractMetadata_shouldMatch(){

        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/container/BigBuckBunny.mp4")
        val tempVideoFile = tmpFolder.newFile("BigBuckBunny.mp4")
        tempVideoFile.copyInputStreamToFile(inputStream)
        val videoUri = DoorUri.parse(tempVideoFile.toURI().toString())
        val processContext = ContentJobProcessContext(videoUri, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val videoPlugin = VideoTypePluginJvm(Any(), Endpoint("http://localhost/dummy"), di)

        runBlocking {
            val metadataResult = videoPlugin.extractMetadata(videoUri, processContext)!!
            Assert.assertEquals("title match video", "BigBuckBunny.mp4", metadataResult.entry.title)
            Assert.assertEquals("contentType is Video", ContentEntry.TYPE_VIDEO, metadataResult.entry.contentTypeFlag)
        }
    }


    @Test
    fun givenValidVideoLink_whenExtractMetadata_shouldMatch(){

        val accountManager: UstadAccountManager by di.instance()
        val videoUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/container/BigBuckBunny.mp4").toString())
        val processContext = ContentJobProcessContext(videoUri, tmpFolder.newFolder().toDoorUri(),
            mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val videoPlugin = VideoTypePluginJvm(Any(), accountManager.activeEndpoint, di)

        runBlocking {
            val metadataResult = videoPlugin.extractMetadata(videoUri, processContext)!!
            Assert.assertEquals("title match video", "BigBuckBunny.mp4", metadataResult.entry.title)
            Assert.assertEquals("contentType is Video", ContentEntry.TYPE_VIDEO, metadataResult.entry.contentTypeFlag)
        }
    }


    @Test
    fun givenInvalidVideoRatio_whenValidateCalled_thenShouldReturnNull() {
        Assert.assertNull("Invalid ratio run through validate ratio function returns null",
            ShrinkUtils.validateRatio("N/A"))
    }

    @Test
    fun givenValidVideoRatio_whenValidateCalled_thenShouldReturnRatio() {
        Assert.assertEquals("Valid ratio returns non-null string", "1:2",
                ShrinkUtils.validateRatio("1:2"))
    }

}