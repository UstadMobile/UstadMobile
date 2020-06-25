package com.ustadmobile.core.controller

//import org.mockito.ArgumentMatchers.any
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.MountedContainerHandler
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.Runnable
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class XapiPackageContentPresenterTest {

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var xapiTmpFile: File

    private var containerDirTmp: File? = null

    private lateinit var xapiContainer: Container

    private var mockedView: XapiPackageContentView? = null


    private var httpd: EmbeddedHTTPD? = null

    private val mountLatch = CountDownLatch(1)

    private val contentEntryUid = 1234L

    @Before
    fun setup() {
        checkJndiSetup()

        context = Any()
        try {
            db = UmAppDatabase.getInstance(context)
            repo = db//.getUmRepository("http://localhost/dummy/", "")
            db.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        xapiContainer = Container().also {
            it.containerContentEntryUid = contentEntryUid
        }
        xapiContainer.containerUid = repo.containerDao.insert(xapiContainer)

        xapiTmpFile = File.createTempFile("testxapipackagecontentpresenter",
                "xapiTmpFile")
        extractTestResourceToFile("/com/ustadmobile/core/contentformats/XapiPackage-JsTetris_TCAPI.zip",
                xapiTmpFile)

        containerDirTmp = UmFileUtilSe.makeTempDir("testxapipackagecontentpresenter",
                "containerDirTmp")
        val containerManager = ContainerManager(xapiContainer, db, repo,
                containerDirTmp!!.absolutePath)
        addEntriesFromZipToContainer(xapiTmpFile.absolutePath, containerManager)

        httpd = EmbeddedHTTPD(0, Any(), db, repo)
        httpd!!.start()
        mockedView = mock{}

        doAnswer { invocation ->
            Thread(invocation.getArgument<Any>(0) as Runnable).start()
            Any()
        }.`when`<XapiPackageContentView>(mockedView).runOnUiThread(any())

    }

    @After
    fun tearDown() {
        xapiTmpFile.delete()
        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
    }


    @Test
    fun givenValidXapiPackage_whenCreated_shouldLoadAndSetTitle() {
        val args = Hashtable<String, String>()
        Assert.assertNotNull(xapiContainer)
        args.put(UstadView.ARG_CONTAINER_UID, xapiContainer.containerUid.toString())
        args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()

        val account = UmAccount(42, "username", "fefe1010fe",
                "http://localhost/")
        val xapiPresenter = XapiPackageContentPresenter(
                context, args, mockedView!!,httpd!!, account)


        xapiPresenter.onCreate(null)

        mountLatch.await(15000, TimeUnit.MILLISECONDS)

        argumentCaptor<String> {
            verify<XapiPackageContentView>(mockedView, timeout(5000)).urlToLoad = capture()
            Assert.assertTrue("Mounted path starts with url and html name",
                    firstValue.startsWith(httpd!!.localHttpUrl) && firstValue.contains("tetris.html"))
            val paramsProvided = UMFileUtil.parseURLQueryString(firstValue)
            val umAccountActor = Json.parse(UmAccountActor.serializer(), paramsProvided["actor"]!!)
            Assert.assertEquals("Account actor is as expected",
                    umAccountActor.account.name, account.username)
            val expectedEndpoint = UMFileUtil.resolveLink(firstValue, "/xapi/$contentEntryUid/")
            Assert.assertEquals("Received expected Xapi endpoint: /xapi/contentEntryUid",
                    expectedEndpoint, paramsProvided["endpoint"])
            Assert.assertEquals("Received expected activity id",
                    "http://id.tincanapi.com/activity/tincan-prototypes/tetris",
                    paramsProvided["activity_id"])
        }

        verify<XapiPackageContentView>(mockedView, timeout(15000)).contentTitle = "Tin Can Tetris Example"
    }

}
