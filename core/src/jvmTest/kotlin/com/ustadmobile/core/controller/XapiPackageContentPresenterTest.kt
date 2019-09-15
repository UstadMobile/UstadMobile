package com.ustadmobile.core.controller

//import org.mockito.ArgumentMatchers.any
import com.nhaarman.mockitokotlin2.any
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.Runnable
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class TestXapiPackageContentPresenter {

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var xapiTmpFile: File

    private var containerDirTmp: File? = null

    private lateinit var xapiContainer: Container

    private var mockXapiPackageContentView: XapiPackageContentView? = null

    private var httpd: EmbeddedHTTPD? = null

    private val xapiXml: TinCanXML? = null

    @Volatile
    private var lastMountedUrl: String? = null

    private val mountLatch = CountDownLatch(1)

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


        xapiContainer = Container()
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

        mockXapiPackageContentView = mock(XapiPackageContentView::class.java)

        doAnswer { invocation ->
            Thread(invocation.getArgument<Any>(0) as Runnable).start()
            Any()
        }.`when`<XapiPackageContentView>(mockXapiPackageContentView).runOnUiThread(any())

    }

    @After
    fun tearDown() {
        xapiTmpFile.delete()
        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenValidXapiPackage_whenCreated_shouldLoadAndSetTitle() {
        val args = Hashtable<String, String>()
        Assert.assertNotNull(xapiContainer)
        args.put(XapiPackageContentView.ARG_CONTAINER_UID, xapiContainer.containerUid.toString())

        val xapiPresenter = XapiPackageContentPresenter(
                context, args, mockXapiPackageContentView!!) {
            val mountedPath = httpd!!.mountContainer(it, null)
            lastMountedUrl = UMFileUtil.joinPaths(httpd!!.localHttpUrl,
                    mountedPath!!)
            mountLatch.countDown()
            lastMountedUrl!!
        }


        xapiPresenter.onCreate(null)

        mountLatch.await(15000, TimeUnit.MILLISECONDS)

        verify<XapiPackageContentView>(mockXapiPackageContentView, timeout(5000)).loadUrl(
                UMFileUtil.joinPaths(lastMountedUrl!!, "tetris.html"))

        verify<XapiPackageContentView>(mockXapiPackageContentView, timeout(15000)).setTitle("Tin Can Tetris Example")
    }

}
