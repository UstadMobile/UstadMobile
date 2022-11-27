package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.impl.nav.navigateToErrorScreen
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.openEntryInputStream
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
import org.mockito.kotlin.*
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.IOException
import java.util.*

class EpubContentPresenterTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    private lateinit var containerDirTmp: File

    private lateinit var mockEpubView: EpubContentView

    private lateinit var epubContainer: Container

    private lateinit var opf: OpfDocument

    lateinit var di: DI

    lateinit var mockStatementEndpoint: XapiStatementEndpoint

    lateinit var contentEntry: ContentEntry

    var selectedClazzUid = 1000L

    @Before
    @Throws(IOException::class, XmlPullParserException::class)
    fun setup() {
        mockStatementEndpoint = mock { }

        di = DI {
            import(ustadTestRule.diModule)
            bind<XapiStatementEndpoint>() with singleton { mockStatementEndpoint }

            bind<XmlSerializer>() with provider {
                KXmlSerializer()
            }

        }

        val accountManager: UstadAccountManager = di.direct.instance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val db: UmAppDatabase by di.activeDbInstance()

        val person = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                personUid = 42L
                username = "user"
                firstNames = "bob"
                lastName = "jones"
            })
        }

        accountManager.startLocalTestSessionBlocking(person, accountManager.activeEndpoint.url)


        contentEntry = ContentEntry("Test epub", "test", true, true).apply {
            contentEntryUid = repo.contentEntryDao.insert(this)
        }

        epubContainer = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = repo.containerDao.insert(this)
        }

        Clazz().apply{
            clazzUid = selectedClazzUid
            repo.clazzDao.insert(this)
        }

        containerDirTmp = tmpFileRule.newFolder("containerDirTmp")
        runBlocking {
            repo.addEntriesToContainerFromZipResource(epubContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/test.epub",
                    ContainerAddOptions(containerDirTmp.toDoorUri()))
        }

        mockEpubView = mock {

            on { runOnUiThread(any()) }.doAnswer {invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }


        //opf var is used when running assertions
        val opfIn = db.containerEntryDao.openEntryInputStream(epubContainer.containerUid, "OEBPS/package.opf")!!
        opf = OpfDocument()
        val xpp = XmlPullParserFactory.newInstance().newPullParser().also {
            it.setInput(opfIn, "UTF-8")
        }
        opf.loadFromOPF(xpp)
        opfIn.close()
    }


    @Suppress("UNCHECKED_CAST")
    @Test
    fun givenValidEpub_whenCreated_shouldSetTitleAndSpineHrefsAndRecordProgress() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()
        args[UstadView.ARG_CLAZZUID] = selectedClazzUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handlePageChanged(2)

        presenter.onStop()

        verify(mockStatementEndpoint, timeout(5000)).storeStatements(argWhere {
            val progressRecorded = it.firstOrNull()?.result?.extensions?.get("https://w3id.org/xapi/cmi5/result/extensions/progress") as? Int
            progressRecorded != null && progressRecorded > 0
        }, anyOrNull(), eq(contentEntry.contentEntryUid), eq(selectedClazzUid))

        argumentCaptor<List<String>>().apply {
            verify(mockEpubView, timeout(20000)).spineUrls = capture()

            val client = HttpClient()
            runBlocking {
                firstValue.forEachIndexed {index, url ->
                    Assert.assertTrue("Spine itemk $index ends with expected url",
                            url.endsWith(opf!!.linearSpineHREFs[index]))

                    val responseStatusCode = client.get(url).status.value
                    Assert.assertEquals("Making HTTP request to spine url status code is 200 OK",
                        200, responseStatusCode)
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
        args[UstadView.ARG_CLAZZUID] = selectedClazzUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handleClickNavItem(EpubNavItem("Link with #", "4.xhtml#anchor", null, 0))

        verify(mockEpubView).scrollToSpinePosition(3, "anchor")
    }

    @Test
    fun givenInvalidEpub_whenLoaded_shouldGoToErrorScreen(){
        val db: UmAppDatabase by di.activeDbInstance()
        db.containerEntryDao.deleteByContainerUid(epubContainer.containerUid)

        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()
        args[UstadView.ARG_CLAZZUID] = selectedClazzUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()

        val spyController: UstadNavController = di.direct.instance()
        verify(spyController, timeout(1000)).navigateToErrorScreen(
                org.mockito.kotlin.eq(Exception()), org.mockito.kotlin.eq(di), org.mockito.kotlin.eq(Any()))

    }


    //@Test
    fun givenValidEpub_whenHandlePageChangeCalledAndTitleIsKnown_thenShouldSetWindowTitle() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()
        args[UstadView.ARG_CLAZZUID] = selectedClazzUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()


        verify(mockEpubView, timeout(15000)).containerTitle = opf!!.title!!

        presenter.handlePageTitleChanged(1, "Title 1")
        presenter.handlePageChanged(1)

        verify(mockEpubView).windowTitle = "Title 1"
    }

    @Test
    fun givenValidEpub_whenHandlePageChangeCalledAndTitleIsUnknown_thenShouldSetWindowTitleFromNavDoc() {
        val args = HashMap<String, String>()
        args[UstadView.ARG_CONTAINER_UID] = epubContainer!!.containerUid.toString()
        args[UstadView.ARG_CONTENT_ENTRY_UID] = contentEntry.contentEntryUid.toString()
        args[UstadView.ARG_CLAZZUID] = selectedClazzUid.toString()

        val presenter = EpubContentPresenter(Any(), args, mockEpubView, di)
        presenter.onCreate(args)
        presenter.onStart()

        verify(mockEpubView, timeout(15000 )).tableOfContents = any()

        presenter.handlePageChanged(1)
        verify(mockEpubView).windowTitle = "Page 2" //the title as specified in test.epub's OPF for this file
    }


}