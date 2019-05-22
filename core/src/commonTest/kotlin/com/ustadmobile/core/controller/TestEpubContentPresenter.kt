///*
//package com.ustadmobile.core.controller
//
//import com.nhaarman.mockitokotlin2.anyArray
//import com.nhaarman.mockitokotlin2.mock
//import com.nhaarman.mockitokotlin2.whenever
//import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.impl.UmCallback
//import com.ustadmobile.core.impl.UmCallbackUtil
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.impl.http.UmHttpRequest
//import com.ustadmobile.core.util.UMFileUtil
//import com.ustadmobile.core.view.EpubContentView
//import com.ustadmobile.lib.db.entities.Container
//import com.ustadmobile.port.sharedse.container.ContainerManager
//import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
//import com.ustadmobile.port.sharedse.util.UmFileUtilSe
//import com.ustadmobile.test.core.impl.PlatformTestUtil
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.mockito.ArgumentMatchers.any
//import org.mockito.ArgumentMatchers.eq
//import org.mockito.Mockito.*
//import org.xmlpull.v1.XmlPullParserException
//import java.io.File
//import java.io.IOException
//import java.util.*
//import java.util.concurrent.atomic.AtomicReference
//import java.util.zip.ZipFile
//
//class TestEpubContentPresenter {
//
//    private var db: UmAppDatabase? = null
//
//    private var repo: UmAppDatabase? = null
//
//    private var epubTmpFile: File? = null
//
//    private var containerDirTmp: File? = null
//
//    private var mockEpubView: EpubContentView? = mock()
//
//    private var epubContainer: Container? = null
//
//    private var httpd: EmbeddedHTTPD? = null
//
//    private var opf: OpfDocument? = null
//
//    @Before
//    @Throws(IOException::class, XmlPullParserException::class)
//    fun setup() {
//        db = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)
//        repo = db!!.getRepository("http://localhost/dummy/", "")
//        db!!.clearAllTables()
//
//        epubContainer = Container()
//        epubContainer!!.containerUid = repo!!.containerDao.insert(epubContainer)
//
//        epubTmpFile = File.createTempFile("testepubcontentpresenter", "epubTmpFile")
//
//        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/epub/test.epub",
//                epubTmpFile)
//
//        containerDirTmp = UmFileUtilSe.makeTempDir("testpubcontentpresenter", "containerDirTmp")
//        val containerManager = ContainerManager(epubContainer, db, repo,
//                containerDirTmp!!.absolutePath)
//
//        val epubZipFile = ZipFile(epubTmpFile!!)
//        containerManager.addEntriesFromZip(epubZipFile, ContainerManager.OPTION_COPY)
//        epubZipFile.close()
//
//        httpd = EmbeddedHTTPD(0, PlatformTestUtil.targetContext, db, repo)
//        httpd!!.start()
//
//        doAnswer {
//            Thread{
//                val mountedUrl = UMFileUtil.joinPaths(httpd!!.localHttpUrl,
//                        httpd!!.mountContainer(it.getArgument(0), ""))
//                UmCallbackUtil.onSuccessIfNotNull(it.getArgument<UmCallback<String>>(1), mountedUrl)
//            }.start()
//            null
//        }.`when`(mockEpubView)?.mountContainer(eq(epubContainer!!.containerUid), any())
//
//
//        doAnswer { invocation ->
//            Thread(invocation.getArgument<Any>(0) as Runnable).start()
//            null!!
//        }.`when`<EpubContentView>(mockEpubView).runOnUiThread(any<Runnable>())
//
//        //Used for verification purposes
//        val opfIn = containerManager.getInputStream(containerManager.getEntry("OEBPS/package.opf"))
//        opf = OpfDocument()
//        opf!!.loadFromOPF(UstadMobileSystemImpl.instance.newPullParser(opfIn, "UTF-8"))
//        opfIn.close()
//    }
//
//    @After
//    fun tearDown() {
//        epubTmpFile!!.delete()
//        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
//    }
//
//    @Test
//    @Throws(IOException::class)
//    fun givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefs() {
//        val args = HashMap<String, String>()
//        args[EpubContentView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
//
//        val hrefListReference = AtomicReference<Any>()
//
//        doAnswer {
//            hrefListReference.set(it.getArgument(0))
//            null!!
//        }.`when`(mockEpubView)?.setSpineUrls(anyArray())
//
//        val presenter = EpubContentPresenter(PlatformTestUtil.targetContext,
//                args, mockEpubView)
//        presenter.onCreate(args)
//
//        verify<EpubContentView>(mockEpubView, timeout(15000)).mountContainer(eq(epubContainer!!.containerUid),
//                any<UmCallback<String>>())
//        verify<EpubContentView>(mockEpubView, timeout(15000)).setContainerTitle(opf!!.title!!)
//
//        verify<EpubContentView>(mockEpubView, timeout(15000)).setSpineUrls(any())
//
//        val linearSpineUrls = hrefListReference.get() as Array<String>
//        for (i in linearSpineUrls.indices) {
//            Assert.assertTrue("Spine itemk $i ends with expected url",
//                    linearSpineUrls[i].endsWith(opf!!.linearSpineHREFs[i]))
//            val response = UstadMobileSystemImpl.instance.makeRequestSync(
//                    UmHttpRequest(PlatformTestUtil.targetContext, linearSpineUrls[i]))
//            Assert.assertEquals("Making HTTP request to spine url status code is 200 OK", 200,
//                    response.status.toLong())
//        }
//
//    }
//
//    @Test
//    fun givenNoOcf_whenCreated_shouldShowErrorMessage() {
//
//    }
//
//}
//*/
