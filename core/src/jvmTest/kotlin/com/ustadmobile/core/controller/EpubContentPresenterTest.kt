package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
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
import org.kodein.di.*
import org.kxml2.io.KXmlSerializer
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
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

    lateinit var mockStatementEndpoint: XapiStatementEndpoint

    lateinit var contentEntry: ContentEntry

    @Before
    @Throws(IOException::class, XmlPullParserException::class)
    fun setup() {
        mockStatementEndpoint = mock { }

        di = DI {
            import(ustadTestRule.diModule)
            bind<XapiStatementEndpoint>() with singleton { mockStatementEndpoint }

            bind<XmlPullParserFactory>() with singleton {
                XmlPullParserFactory.newInstance().also {
                    it.isNamespaceAware = true
                }
            }

            bind<XmlPullParser>() with provider {
                instance<XmlPullParserFactory>().newPullParser()
            }

            bind<XmlSerializer>() with provider {
                KXmlSerializer()
            }

        }

        val accountManager: UstadAccountManager = di.direct.instance()
        accountManager.activeAccount = UmAccount(42L, "user", "",
                "http://localhost:4200/", "bob", "jones")

        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        contentEntry = ContentEntry("Test epub", "test", true, true).apply {
            contentEntryUid = db.contentEntryDao.insert(this)
        }

        epubContainer = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = repo.containerDao.insert(this)
        }

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
    fun givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefsAndRecordProgress() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handlePageChanged(2)

        presenter.onStop()

        verify(mockStatementEndpoint, timeout(5000)).storeStatements(argWhere {
            val progressRecorded = it.firstOrNull()?.result?.extensions?.get("https://w3id.org/xapi/cmi5/result/extensions/progress") as? Int
            progressRecorded != null && progressRecorded > 0
        }, anyOrNull(), eq(contentEntry.contentEntryUid))

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


    @Test
    fun givenValidEpubAndNavItemExists_whenHandleClickNavItemCalled_thenShouldCallScrollToSpinePosition() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handleClickNavItem(EpubNavItem("Link with #", "4.xhtml#anchor", null, 0))

        verify(mockEpubView).scrollToSpinePosition(3, "anchor")
    }

    @Test
    fun givenValidEpub_whenHandlePageChangeCalledAndTitleIsKnown_thenShouldSetWindowTitle() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handlePageTitleChanged(1, "Page 1")
        presenter.handlePageChanged(1)

        //This should actually come from the table of contents
        verify(mockEpubView, timeout(5000).atLeastOnce()).windowTitle = "Page 1"
    }

    @Test
    fun givenValidEpub_whenHandlePageChangeCalledAndTitleIsUnknown_thenShouldSetWindowTitleFromNavDoc() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()

        verify(mockEpubView, timeout(15000)).tableOfContents = any()

        presenter.handlePageChanged(1)
        verify(mockEpubView).windowTitle = "Page 2" //the title as specified in test.epub's OPF for this file
    }


}