package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipFile

class EpubContentPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var epubTmpFile: File

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    private lateinit var containerDirTmp: File

    private lateinit var mockEpubView: EpubContentView

    private var epubContainer: Container? = null

    private var opf: OpfDocument? = null

    lateinit var di: DI

    @Before
    @Throws(IOException::class, XmlPullParserException::class)
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        epubContainer = Container()
        epubContainer!!.containerUid = repo.containerDao.insert(epubContainer!!)

        epubTmpFile = tmpFileRule.newFile("epubTmpFile")

        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/core/contentformats/epub/test.epub",
                epubTmpFile)

        containerDirTmp = tmpFileRule.newFolder("containerDirTmp")
        val containerManager = ContainerManager(epubContainer!!, db, repo,
                containerDirTmp.absolutePath)

        val epubZipFile = ZipFile(epubTmpFile)
        addEntriesFromZipToContainer(epubTmpFile.absolutePath, containerManager)
        epubZipFile.close()

        mockEpubView = mock {

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


    @Suppress("UNCHECKED_CAST")
    @Test
    @Throws(IOException::class)
    fun givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefs() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        argumentCaptor<List<String>>().apply {
            verify(mockEpubView, timeout(20000)).spineUrls = capture()

            val client = HttpClient()
            runBlocking {
                firstValue.forEachIndexed {index, url ->
                    Assert.assertTrue("Spine itemk $index ends with expected url",
                            url.endsWith(opf!!.linearSpineHREFs[index]))

                    val responseStatusCode = client.get<HttpStatement>(url).execute {
                        it.status.value
                    }
                    Assert.assertEquals("Making HTTP request to spine url status code is 200 OK", 200,
                            responseStatusCode)
                }
            }
            client.close()
        }

    }

}