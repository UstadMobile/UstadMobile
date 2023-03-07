package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.util.*
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assume
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.util.concurrent.TimeUnit

class VideoTypePluginJvmTest {


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

    private lateinit var activeEndpoint: Endpoint

    @Before
    fun setup(){

        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
            bind<File>(tag = DiTag.TAG_FILE_FFMPEG) with singleton {
                SysPathUtil.findCommandInPath("ffmpeg")
                    ?: throw IllegalStateException("Could not find ffmpeg. " +
                            "ffmpeg must be in path to run VideoTypePluginJvmTest")
            }

            bind<File>(tag = DiTag.TAG_FILE_FFPROBE) with singleton {
                SysPathUtil.findCommandInPath("ffprobe")
                    ?: throw IllegalStateException("ffprobe must be in path to run VideoTypePluginJvmTest")
            }

            bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
                ContainerStorageManager(listOf(tmpFolder.newFolder()))
            }
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()

        activeEndpoint = di.direct.instance<UstadAccountManager>().activeEndpoint
        db = di.onActiveAccountDirect().instance(tag = DoorTag.TAG_DB)
    }

    private fun importVideoUsingContentJobAndAssertValidOutput(
        sourceUri: DoorUri,
        compress: Boolean,
    ){
        val processContext = ContentJobProcessContext(sourceUri, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf("compress" to compress.toString()), DummyContentJobItemTransactionRunner(db), di)

        val jobItem = ContentJobItem(sourceUri = sourceUri.toString(),
            cjiParentContentEntryUid = 42, cjiContentEntryUid = 42)
        val job = ContentJob(toUri = tmpFolder.newFolder().toDoorUri().toString()).also {
            it.params = Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                mapOf("compress" to "true"))
        }
        val jobAndItem = ContentJobItemAndContentJob().apply{
            this.contentJob = job
            this.contentJobItem = jobItem
        }

        runBlocking {
            db.contentJobItemDao.insertJobItem(jobItem)
            db.contentJobDao.insertAsync(job)
        }

        val videoPlugin = VideoTypePluginJvm(Any(), activeEndpoint, di)

        runBlocking {
            videoPlugin.processJob(jobAndItem, processContext,  { })
        }

        val container = runBlocking {
            db.containerDao.findContainersForContentEntryUid(42).first()
        }

        val containerEntries = db.containerEntryDao.findByContainer(container.containerUid)
        Assert.assertEquals("Container has one entry", 1, containerEntries.size)
        Assert.assertEquals("Video inside container is not compressed",
            ContainerEntryFile.COMPRESSION_NONE,
            containerEntries.first().containerEntryFile?.compression ?: -1)
        Assert.assertEquals("Container entry ends with correct file extension",
            "mp4", containerEntries.first().cePath?.substringAfterLast("."))

        val ffprobeCommand = SysPathUtil.findCommandInPath("ffprobe")
        Assume.assumeNotNull(ffprobeCommand)

        val processBuilder = ProcessBuilder(ffprobeCommand!!.absolutePath,
            containerEntries.first().containerEntryFile?.cefPath)
        val ffprobeExitVal = processBuilder.start().let {
            it.waitFor(5, TimeUnit.SECONDS)
            it.exitValue()
        }
        Assert.assertEquals("ffprobe indicates valid file", 0, ffprobeExitVal)
    }

    @Test
    fun givenValidVideoCompressionEnabled_whenImportedFromFileCalled_thenShouldAddToContainer() {
        val tempVideoFile = tmpFolder.newFile("BigBuckBunny.mp4")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4")!!
            .writeToFile(tempVideoFile)

        importVideoUsingContentJobAndAssertValidOutput(tempVideoFile.toDoorUri(), true)
    }

    @Test
    fun givenValidVideoCompressionDisabled_whenImportedFromFileCalled_thenShouldAddToContainer() {
        val tempVideoFile = tmpFolder.newFile("BigBuckBunny.mp4")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4")!!
            .writeToFile(tempVideoFile)

        importVideoUsingContentJobAndAssertValidOutput(tempVideoFile.toDoorUri(), false)
    }

    @Test
    fun givenValidVideoCompressionEnabled_whenImportedFromHttpUrl_thenShouldAddToContainer() {
        importVideoUsingContentJobAndAssertValidOutput(DoorUri.parse(
            mockWebServer.url("/com/ustadmobile/core/container/BigBuckBunny.mp4").toString()), true)
    }

    @Test
    fun givenValidVideoCompressionDisabled_whenImportedFromHttpUrl_thenShouldAddToContainer() {
        importVideoUsingContentJobAndAssertValidOutput(DoorUri.parse(
            mockWebServer.url("/com/ustadmobile/core/container/BigBuckBunny.mp4").toString()), false)
    }


    @Test
    fun givenValidVideo_whenExtractMetadata_shouldMatch(){
        val ffprobePath = di.direct.instance<File>(tag = DiTag.TAG_FILE_FFPROBE)
        println(ffprobePath.absolutePath)

        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/container/BigBuckBunny.mp4")!!
        val tempVideoFile = tmpFolder.newFile("BigBuckBunny.mp4")
        tempVideoFile.copyInputStreamToFile(inputStream)
        val videoUri = DoorUri.parse(tempVideoFile.toURI().toString())
        val processContext = ContentJobProcessContext(videoUri, tmpFolder.newFolder().toDoorUri(),
            params = mutableMapOf(), DummyContentJobItemTransactionRunner(db), di)

        val videoPlugin = VideoTypePluginJvm(Any(), Endpoint("http://localhost/dummy"), di)

        runBlocking {
            val metadataResult = videoPlugin.extractMetadata(videoUri, processContext)!!
            Assert.assertEquals("title match video", "BigBuckBunny.mp4",
                metadataResult.entry.title)
            Assert.assertEquals("contentType is Video", ContentEntry.TYPE_VIDEO,
                metadataResult.entry.contentTypeFlag)
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
            Assert.assertEquals("title match video", "BigBuckBunny.mp4",
                metadataResult.entry.title)
            Assert.assertEquals("contentType is Video", ContentEntry.TYPE_VIDEO,
                metadataResult.entry.contentTypeFlag)
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