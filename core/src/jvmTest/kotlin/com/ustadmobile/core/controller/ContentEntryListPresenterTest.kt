package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.controller.ContentEntryListPresenter.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ContentEntryListPresenterTest {

    private lateinit var libraryContent1: ContentEntry
    private lateinit var rootEntry: ContentEntry
    private lateinit var mockView: ContentEntryListView

    private var args = HashMap<String, String>()

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppRepository: UmAppDatabase

    private lateinit var contentEntryDao: ContentEntryDao

    private lateinit var contentEntryRepoDao: ContentEntryDao

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var activeAccount: UmAccount


    @Before
    fun setup() {
        checkJndiSetup()
        mockView = mock {
            on { runOnUiThread(org.mockito.ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }

        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppRepository = umAppDatabase //for this test there is no difference
        umAppDatabase.clearAllTables()
        umAppRepository.clearAllTables()

        contentEntryDao = mock()
        contentEntryRepoDao = mock()

        activeAccount = mock()

        systemImpl = mock()

        rootEntry = ContentEntry()
        rootEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(rootEntry)

        var downloadedEntry = ContentEntry()
        downloadedEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(downloadedEntry)

        var recycledEntry = ContentEntry()
        recycledEntry.ceInactive = true
        recycledEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(recycledEntry)

        libraryContent1 = ContentEntry()
        libraryContent1.contentEntryUid = umAppDatabase.contentEntryDao.insert(libraryContent1)

        var libraryContent2 = ContentEntry()
        libraryContent2.contentEntryUid = umAppDatabase.contentEntryDao.insert(libraryContent2)

        var rootdownloadJoin = ContentEntryParentChildJoin()
        rootdownloadJoin.cepcjParentContentEntryUid = rootEntry.contentEntryUid
        rootdownloadJoin.cepcjChildContentEntryUid = downloadedEntry.contentEntryUid
        rootdownloadJoin.cepcjUid = umAppDatabase.contentEntryParentChildJoinDao.insert(rootdownloadJoin)

        var rootrecycleJoin = ContentEntryParentChildJoin()
        rootrecycleJoin.cepcjParentContentEntryUid = rootEntry.contentEntryUid
        rootrecycleJoin.cepcjChildContentEntryUid = recycledEntry.contentEntryUid
        rootrecycleJoin.cepcjUid = umAppDatabase.contentEntryParentChildJoinDao.insert(rootrecycleJoin)

        var rootlib1Join = ContentEntryParentChildJoin()
        rootlib1Join.cepcjParentContentEntryUid = rootEntry.contentEntryUid
        rootlib1Join.cepcjChildContentEntryUid = libraryContent1.contentEntryUid
        rootlib1Join.cepcjUid = umAppDatabase.contentEntryParentChildJoinDao.insert(rootlib1Join)

        var rootlib2Join = ContentEntryParentChildJoin()
        rootlib2Join.cepcjParentContentEntryUid = rootEntry.contentEntryUid
        rootlib2Join.cepcjChildContentEntryUid = libraryContent2.contentEntryUid
        rootlib2Join.cepcjUid = umAppDatabase.contentEntryParentChildJoinDao.insert(rootlib2Join)

        var downloadJob = DownloadJob()
        downloadJob.djRootContentEntryUid = downloadedEntry.contentEntryUid
        downloadJob.djStatus = JobStatus.COMPLETE
        downloadJob.djUid = umAppDatabase.downloadJobDao.insert(downloadJob).toInt()

        var downloadEntryStatus = ContentEntryStatus()
        downloadEntryStatus.cesUid = downloadedEntry.contentEntryUid
        umAppDatabase.contentEntryStatusDao.insert(downloadEntryStatus)

        var downloadContainer = Container()
        downloadContainer.containerContentEntryUid = downloadedEntry.contentEntryUid
        downloadContainer.cntLastModified = System.currentTimeMillis()
        downloadContainer.containerUid = umAppDatabase.containerDao.insert(downloadContainer)

        var recycledContainer = Container()
        recycledContainer.containerContentEntryUid = recycledEntry.contentEntryUid
        recycledContainer.cntLastModified = System.currentTimeMillis()
        recycledContainer.containerUid = umAppDatabase.containerDao.insert(recycledContainer)

        args[UstadView.ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
    }


    @Test
    fun givenArgToViewDownloadedContent_whenOnCreateCalled_thenShouldSetListOfDownloadedContent() {

        args[ARG_DOWNLOADED_CONTENT] = ""
        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

        verify(contentEntryDao, timeout(5000)).downloadedRootItems()
        verify(mockView, timeout(5000)).setContentEntryProvider(any())
    }

    @Test
    fun givenArgToViewRecycledContent_whenOnCreateCalled_thenShouldSetListOfRecycledContent() {

        args[ARG_RECYCLED_CONTENT] = ""
        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

        verify(contentEntryRepoDao, timeout(5000)).recycledItems()
        verify(mockView, timeout(5000)).setContentEntryProvider(any())

    }

    @Test
    fun givenArgToViewLibraryContent_whenOnCreateCalled_thenShouldSetListOfLibraryContentAndShowFiltersOfLanguagesAndCategory() {
        runBlocking {

            var categoryTest = DistinctCategorySchema()
            categoryTest.contentCategorySchemaUid = 2323L
            categoryTest.schemaName = "Test"

            val contentEntryLiveData = spy(DoorMutableLiveData(rootEntry as ContentEntry?))


            val repoContentEntryDaoSpy = spy(umAppRepository.contentEntryDao) {
                on { findLiveContentEntry(rootEntry.contentEntryUid) }.thenReturn(contentEntryLiveData)
                on {
                    runBlocking {
                        findUniqueLanguagesInListAsync(rootEntry.contentEntryUid)
                    }
                }.thenReturn(listOf(Language()))
                on {
                    runBlocking {
                        findListOfCategoriesAsync(rootEntry.contentEntryUid)
                    }
                }.thenReturn(listOf(categoryTest))
            }

            val repoSpy = spy(umAppRepository) {
                on { contentEntryDao }.thenReturn(repoContentEntryDaoSpy)
            }

            val selectLang = Language()
            selectLang.name = "Language"
            selectLang.langUid = 0

            val allLang = Language()
            allLang.name = "All"
            allLang.langUid = 0

            val schemaTitle = DistinctCategorySchema()
            schemaTitle.categoryName = categoryTest.schemaName
            schemaTitle.contentCategoryUid = 0
            schemaTitle.contentCategorySchemaUid = 0

            val allSchema = DistinctCategorySchema()
            allSchema.categoryName = "All"
            allSchema.contentCategoryUid = 0
            allSchema.contentCategorySchemaUid = 0


            args[ARG_LIBRARIES_CONTENT] = ""
            var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, repoContentEntryDaoSpy, activeAccount, systemImpl, repoSpy)
            presenter.onCreate(null)

            verify(repoContentEntryDaoSpy, timeout(5000)).getChildrenByParentUidWithCategoryFilter(eq(rootEntry.contentEntryUid), eq(0), eq(0), eq(0))
            verify(mockView, timeout(5000)).setContentEntryProvider(any())
            verify(contentEntryLiveData).observe(any(), any())

            verify(repoContentEntryDaoSpy, timeout(5000)).findUniqueLanguagesInListAsync(eq(rootEntry.contentEntryUid))
            verify(mockView, timeout(5000)).setLanguageOptions(eq(listOf(selectLang, allLang, Language())))

            verify(repoContentEntryDaoSpy, timeout(5000)).findListOfCategoriesAsync(eq(rootEntry.contentEntryUid))
            verify(mockView, timeout(5000)).setCategorySchemaSpinner(eq(mapOf(2323L to listOf(schemaTitle, allSchema, categoryTest))))


        }


    }

    @Test
    fun givenListOfLanguages_whenUserSelectsEnglish_thenFilterAndShowListOfEnglishContent() {

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

    }

    @Test
    fun givenListOfCategory_whenUserSelectsACategory_thenFilterAndShowListOfCategoryContent() {

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
    }

    @Test
    fun givenListOfContent_whenUserClicksOnDownloadIcon_thenShouldOpenDownloadDialog() {
        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

    }

    @Test
    fun givenListOfContent_whenUserClicksOnLeafContent_thenShouldOpenContentEntryDetail() {
        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

    }

    @Test
    fun givenListOfContent_whenUserClicksOnEntryNotLeaf_thenShouldOpenContentEntryListActivity() {
        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

    }

    @Test
    fun givenUserIsAdmin_whenClicksOnAddContentIcon_thenShouldOpenContentEntryEditActivity() {

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
    }

    @Test
    fun givenUserIsAdmin_whenClicksOnEditContent_thenShouldOpenContentEntryEditActivity() {

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, activeAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)

    }


}