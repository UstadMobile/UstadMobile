package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.container.ContainerManager
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import com.ustadmobile.test.core.util.checkJndiSetup

import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

//import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import java.io.FileReader
import java.nio.file.Paths
import java.util.*


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
            repo = db//.getRepository("http://localhost/dummy/", "")
            db.clearAllTables()
        }catch(e: Exception) {
            e.printStackTrace()
        }


        xapiContainer = Container()
        xapiContainer.containerUid = repo.containerDao.insert(xapiContainer)

        xapiTmpFile = File.createTempFile("testxapipackagecontentpresenter",
                "xapiTmpFile")
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/XapiPackage-JsTetris_TCAPI.zip",
                xapiTmpFile)

        containerDirTmp = UmFileUtilSe.makeTempDir("testxapipackagecontentpresenter",
                "containerDirTmp")
        val containerManager = ContainerManager(xapiContainer, db, repo,
                containerDirTmp!!.absolutePath)
        val xapiZipFile = ZipFile(xapiTmpFile)
        containerManager.addEntriesFromZip(xapiZipFile, ContainerManager.OPTION_COPY)
        xapiZipFile.close()

        httpd = EmbeddedHTTPD(0, Any(), db, repo)
        httpd!!.start()

//        mockXapiPackageContentView = mock(XapiPackageContentView::class.java)
//        doAnswer {
//            Thread {
//                lastMountedUrl = UMFileUtil.joinPaths(httpd!!.localHttpUrl,
//                        httpd!!.mountContainer(it.getArgument(0)!!, null)!!)
//                UmCallbackUtil.onSuccessIfNotNull<String>(it.getArgument<UmCallback<String>>(1),
//                        lastMountedUrl!!)
//                mountLatch.countDown()
//            }.start()
//            Any()
//        }.`when`<XapiPackageContentView>(mockXapiPackageContentView)
//                .mountContainer(eq(xapiContainer.containerUid), any<UmCallback<String>>())
//
//
//        doAnswer { invocation ->
//            Thread(invocation.getArgument<Any>(0) as Runnable).start()
//            Any()
//        }.`when`<XapiPackageContentView>(mockXapiPackageContentView).runOnUiThread(any())

    }

    @After
    fun tearDown() {
        xapiTmpFile.delete()
        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenValidXapiPackage_whenCreated_shouldLoadAndSetTitle() {
//        val args = Hashtable<String,String>()
        Assert.assertNotNull(xapiContainer)
//        args.put(XapiPackageContentView.ARG_CONTAINER_UID, xapiContainer.containerUid.toString())
//
//        val xapiPresenter = XapiPackageContentPresenter(
//                context, args, mockXapiPackageContentView!!)
//        xapiPresenter.onCreate(null)
//
//        mountLatch.await(15000, TimeUnit.MILLISECONDS)
//
//        verify<XapiPackageContentView>(mockXapiPackageContentView).mountContainer(
//                eq(xapiContainer.containerUid), any<UmCallback<String>>())
//
//        verify<XapiPackageContentView>(mockXapiPackageContentView, timeout(5000)).loadUrl(
//                UMFileUtil.joinPaths(lastMountedUrl!!, "tetris.html"))
//
//        verify<XapiPackageContentView>(mockXapiPackageContentView, timeout(15000)).setTitle("Tin Can Tetris Example")
    }

}
