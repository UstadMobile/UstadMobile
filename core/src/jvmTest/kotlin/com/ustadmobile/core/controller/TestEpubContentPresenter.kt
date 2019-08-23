package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.anyArray
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipFile
import com.ustadmobile.core.util.UMFileUtil.joinPaths
import kotlinx.coroutines.runBlocking

class TestEpubContentPresenter {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private var epubTmpFile: File? = null

    private var containerDirTmp: File? = null

    private lateinit var mockEpubView: EpubContentView

    private var epubContainer: Container? = null

    private lateinit var httpd: EmbeddedHTTPD

    private var opf: OpfDocument? = null

    @Before
    @Throws(IOException::class, XmlPullParserException::class)
    fun setup() {
        db = UmAppDatabase.getInstance(Any())
        repo = db
        db.clearAllTables()

        epubContainer = Container()
        epubContainer!!.containerUid = repo.containerDao.insert(epubContainer!!)

        epubTmpFile = File.createTempFile("testepubcontentpresenter", "epubTmpFile")

        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/epub/test.epub",
                epubTmpFile!!)

        containerDirTmp = UmFileUtilSe.makeTempDir("testpubcontentpresenter", "containerDirTmp")
        val containerManager = ContainerManager(epubContainer!!, db, repo,
                containerDirTmp!!.absolutePath)

        val epubZipFile = ZipFile(epubTmpFile!!)
        addEntriesFromZipToContainer(epubTmpFile!!.absolutePath, containerManager)
        epubZipFile.close()

        httpd = EmbeddedHTTPD(0, Any(), db, repo)
        httpd.start()

        mockEpubView = mock {
            on { mountContainer(eq(epubContainer!!.containerUid), any()) }.doAnswer {
                val mountedUrl = joinPaths(httpd.localHttpUrl,
                        httpd.mountContainer(it.getArgument(0),null)!!)
                UmCallbackUtil.onSuccessIfNotNull(it.getArgument<UmCallback<String>>(1), mountedUrl)
                Unit
            }

            on { runOnUiThread(any()) }.doAnswer {invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }

        //Used for verification purposes
        val opfIn = containerManager.getInputStream(containerManager.getEntry("OEBPS/package.opf")!!)
        opf = OpfDocument()
        opf!!.loadFromOPF(UstadMobileSystemImpl.instance.newPullParser(opfIn, "UTF-8"))
        opfIn.close()
    }

    @After
    fun tearDown() {
        epubTmpFile!!.delete()
        UmFileUtilSe.deleteRecursively(containerDirTmp!!)
    }

    @Test
    @Throws(IOException::class)
    fun givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefs() {
        val args = HashMap<String, String>()
        args[EpubContentView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()

        val hrefListReference = AtomicReference<Any>()

        doAnswer {
            hrefListReference.set(it.getArgument(0))
            Unit
        }.`when`(mockEpubView)?.setSpineUrls(anyArray(), eq(0))

        val presenter = EpubContentPresenter(Any(),
                args, mockEpubView)
        presenter.onCreate(args)

        verify<EpubContentView>(mockEpubView, timeout(15000)).mountContainer(eq(epubContainer!!.containerUid),
                any<UmCallback<String>>())
        verify<EpubContentView>(mockEpubView, timeout(15000)).setContainerTitle(opf!!.title!!)

        verify<EpubContentView>(mockEpubView, timeout(15000)).setSpineUrls(any(), eq(0))

        val linearSpineUrls = hrefListReference.get() as Array<String>
        val client = HttpClient()
        runBlocking {
            for (i in linearSpineUrls.indices) {
                Assert.assertTrue("Spine itemk $i ends with expected url",
                        linearSpineUrls[i].endsWith(opf!!.linearSpineHREFs[i]))

                val response = client.get<HttpResponse>(linearSpineUrls[i])
                Assert.assertEquals("Making HTTP request to spine url status code is 200 OK", 200,
                        response.status.value)
            }
        }

        client.close()

    }

    @Test
    fun givenNoOcf_whenCreated_shouldShowErrorMessage() {

    }

}
