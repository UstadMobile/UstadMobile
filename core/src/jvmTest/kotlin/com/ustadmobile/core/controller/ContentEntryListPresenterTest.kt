package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_FILTER_BUTTONS
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ContentEntryListPresenterTest {

    private lateinit var adminAccount: UmAccount
    private lateinit var rootEntry: ContentEntry
    private lateinit var mockView: ContentEntryListView

    private val context = mock<DoorLifecycleOwner>() {
        on { currentState }.thenReturn(DoorLifecycleObserver.STARTED)
    } as Any

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppRepository: UmAppDatabase

    private lateinit var contentEntryDao: ContentEntryDao

    private lateinit var contentEntryRepoDao: ContentEntryDao

    private lateinit var systemImpl: UstadMobileSystemImpl

    private var mockAccount: UmAccount? = null

    private val args = mutableMapOf(
            ARG_FILTER_BUTTONS to "$ARG_LIBRARIES_CONTENT,$ARG_DOWNLOADED_CONTENT,$ARG_RECYCLED_CONTENT")


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

        mockAccount = mock()
        adminAccount = UmAccount(1, "test", "", "")

        systemImpl = mock {
            on {
                getAppConfigString(AppConfig.KEY_NO_IFRAME, "", context)
            }.thenReturn("example.com")
        }

        rootEntry = ContentEntry()
        rootEntry.contentEntryUid = umAppDatabase.contentEntryDao.insert(rootEntry)

        args[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
    }


    @Test
    fun givenArgToViewDownloadedContent_whenOnCreateCalled_thenShouldSetListOfDownloadedContent() {

        val contentEntryDaoSpy = spy(umAppDatabase.contentEntryDao)
        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDaoSpy, contentEntryRepoDao, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        presenter.handleClickFilterButton(1)

        verify(contentEntryDaoSpy, timeout(5000)).downloadedRootItems()
        verify(mockView, timeout(5000)).setContentEntryProvider(any())
    }

    @Test
    fun givenArgToViewRecycledContent_whenOnCreateCalled_thenShouldSetListOfRecycledContent() {

        val repoContentEntryDaoSpy = spy(umAppRepository.contentEntryDao)
        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, repoContentEntryDaoSpy, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        presenter.handleClickFilterButton(2)

        verify(repoContentEntryDaoSpy, timeout(5000)).recycledItems()
        verify(mockView, timeout(5000)).setContentEntryProvider(any())

    }

    @Test
    fun givenArgToViewLibraryContent_whenOnCreateCalled_thenShouldSetListOfLibraryContentAndShowFiltersOfLanguagesAndCategory() {
        runBlocking {

            val categoryTest = DistinctCategorySchema()
            categoryTest.contentCategorySchemaUid = 2323L
            categoryTest.schemaName = "Test"

            val englishLang = LangUidAndName()

            val spanishLang = LangUidAndName()

            val contentEntryLiveData = spy(DoorMutableLiveData(rootEntry as ContentEntry?))

            val repoContentEntryDaoSpy = spy(umAppRepository.contentEntryDao) {
                on { findLiveContentEntry(rootEntry.contentEntryUid) }.thenReturn(contentEntryLiveData)
                on {
                    runBlocking {
                        findUniqueLanguageWithParentUid(rootEntry.contentEntryUid)
                    }
                }.thenReturn(listOf(englishLang, spanishLang))
                on {
                    runBlocking {
                        findListOfCategoriesAsync(rootEntry.contentEntryUid)
                    }
                }.thenReturn(listOf(categoryTest))
            }

            val repoSpy = spy(umAppRepository) {
                on { contentEntryDao }.thenReturn(repoContentEntryDaoSpy)
            }

            val dbContentEntryDaoSpy = spy(umAppDatabase.contentEntryDao) {
                on {
                    runBlocking {
                        findUniqueLanguageWithParentUid(rootEntry.contentEntryUid)
                    }
                }.thenReturn(listOf(englishLang, spanishLang))
            }

            val selectLang = LangUidAndName()
            selectLang.langName = "Language"
            selectLang.langUid = 0

            val allLang = LangUidAndName()
            allLang.langName = "All"
            allLang.langUid = 0

            val schemaTitle = DistinctCategorySchema()
            schemaTitle.categoryName = categoryTest.schemaName
            schemaTitle.contentCategoryUid = 0
            schemaTitle.contentCategorySchemaUid = 0

            val allSchema = DistinctCategorySchema()
            allSchema.categoryName = "All"
            allSchema.contentCategoryUid = 0
            allSchema.contentCategorySchemaUid = 0


            var presenter = ContentEntryListPresenter(context, args, mockView, dbContentEntryDaoSpy, repoContentEntryDaoSpy, mockAccount, systemImpl, repoSpy)
            presenter.onCreate(null)
            presenter.handleClickFilterButton(0)

            verify(repoContentEntryDaoSpy, timeout(5000)).getChildrenByParentUidWithCategoryFilter(eq(rootEntry.contentEntryUid), eq(0), eq(0), eq(0))
            verify(mockView, timeout(5000)).setContentEntryProvider(any())
            verify(contentEntryLiveData).observe(any(), any())

            verify(repoContentEntryDaoSpy, timeout(5000)).findUniqueLanguageWithParentUid(eq(rootEntry.contentEntryUid))
            verify(mockView, timeout(5000)).setLanguageOptions(eq(listOf(selectLang, allLang, englishLang, spanishLang)))

            verify(repoContentEntryDaoSpy, timeout(5000)).findListOfCategoriesAsync(eq(rootEntry.contentEntryUid))
            verify(mockView, timeout(5000)).setCategorySchemaSpinner(eq(mapOf(categoryTest.contentCategorySchemaUid to listOf(schemaTitle, allSchema, categoryTest))))

        }


    }

    @Test
    fun givenListOfLanguages_whenUserSelectsEnglish_thenFilterAndShowListOfEnglishContent() {

        val repoContentEntryDaoSpy = spy(umAppRepository.contentEntryDao)

        val englishLang = LangUidAndName()

        val spanishLang = LangUidAndName()

        val dbContentEntryDaoSpy = spy(umAppDatabase.contentEntryDao) {
            on {
                runBlocking {
                    findUniqueLanguageWithParentUid(rootEntry.contentEntryUid)
                }
            }.thenReturn(listOf(englishLang, spanishLang))
        }


        val presenter = ContentEntryListPresenter(context, args, mockView, dbContentEntryDaoSpy, repoContentEntryDaoSpy, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        presenter.handleClickFilterByLanguage(1L)
        verify(repoContentEntryDaoSpy, timeout(5000)).getChildrenByParentUidWithCategoryFilter(any(), eq(1L), eq(0), eq(0))
        verify(mockView, timeout(5000)).setContentEntryProvider(any())

    }

    @Test
    fun givenListOfCategory_whenUserSelectsACategory_thenFilterAndShowListOfCategoryContent() {

        val repoContentEntryDaoSpy = spy(umAppRepository.contentEntryDao)

        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, repoContentEntryDaoSpy, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        presenter.handleClickFilterByCategory(1L)
        verify(repoContentEntryDaoSpy, timeout(5000)).getChildrenByParentUidWithCategoryFilter(eq(rootEntry.contentEntryUid), eq(0), eq(1L), eq(0))
        verify(mockView, timeout(5000)).setContentEntryProvider(any())
    }

    @Test
    fun givenListOfContent_whenUserClicksOnDownloadIcon_thenShouldOpenDownloadDialog() {
        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        presenter.handleDownloadStatusButtonClicked(rootEntry)
        verify(systemImpl, timeout(5000)).go(eq("DownloadDialog"), eq(mapOf(ARG_CONTENT_ENTRY_UID to rootEntry.contentEntryUid.toString())), eq(context))
    }

    @Test
    fun givenListOfContent_whenUserClicksOnLeafContent_thenShouldOpenContentEntryDetail() {

        val arguments = mutableMapOf<String, String>()
        args.remove(ARG_FILTER_BUTTONS)
        arguments.putAll(args)
        arguments[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
        arguments[ContentEntryListPresenter.ARG_NO_IFRAMES] = "false"
        arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()

        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, mockAccount, systemImpl, umAppRepository)
        presenter.onCreate(null)
        rootEntry.leaf = true
        presenter.handleContentEntryClicked(rootEntry)

        verify(systemImpl).go(eq(ContentEntryDetailView.VIEW_NAME), eq(arguments), eq(context))
    }

    @Test
    fun givenListOfContent_whenUserClicksOnEntryNotLeaf_thenShouldOpenContentEntryListActivity() {

        val arguments = mutableMapOf<String, String>()
        args.remove(ARG_FILTER_BUTTONS)
        arguments.putAll(args)
        arguments[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
        arguments[ContentEntryListPresenter.ARG_NO_IFRAMES] = "false"
        arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()

        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, mockAccount, systemImpl, umAppRepository)

        presenter.handleContentEntryClicked(rootEntry)

        verify(systemImpl).go(eq(ContentEntryListView.VIEW_NAME), eq(arguments), any())

    }

    @Test
    fun givenUserIsAdmin_whenClicksOnAddContentIconAndSelectsCreateFolder_thenShouldOpenContentEntryEditActivity() {

        val personDaoSpy = spy(umAppRepository.personDao)

        val repo = spy(umAppRepository){
            on { personDao }.thenReturn(personDaoSpy)
        }

        runBlocking {
            val person = Person()
            person.admin = true
            whenever(personDaoSpy.findByUid(any())).thenReturn(person)

        }
        args[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()
        val arguments = mutableMapOf<String, String>()
        arguments.putAll(args)
        arguments[ARG_CONTENT_ENTRY_UID] = 0.toString()
        arguments[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = rootEntry.contentEntryUid.toString()
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_FOLDER.toString()
        arguments[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()

        val presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, adminAccount, systemImpl, repo)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).setEditButtonsVisibility(eq(arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG]!!.toInt()))

        presenter.handleClickAddContent(ContentEntryListView.CONTENT_CREATE_FOLDER)

        verify(systemImpl, timeout(5000)).go(eq(ContentEntryEditView.VIEW_NAME), eq(arguments), eq(context))

    }

    @Test
    fun givenUserIsAdmin_whenClicksOnAddContentIconAndSelectsImportLink_thenShouldOpenContentEntryEditActivity() {


        var personDaoSpy = spy(umAppRepository.personDao)

        var repo = spy(umAppRepository){
            on { personDao }.thenReturn(personDaoSpy)
        }

        runBlocking {
            var person = Person()
            person.admin = true
            whenever(personDaoSpy.findByUid(any())).thenReturn(person)

        }

        args[ARG_LIBRARIES_CONTENT] = ""
        args[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()
        var arguments = mutableMapOf<String, String>()
        arguments.putAll(args)
        arguments[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = rootEntry.contentEntryUid.toString()
        arguments[ARG_CONTENT_ENTRY_UID] = 0.toString()
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_IMPORT_LINK.toString()
        arguments[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, adminAccount, systemImpl, repo)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).setEditButtonsVisibility(eq(arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG]!!.toInt()))

        presenter.handleClickAddContent(ContentEntryListView.CONTENT_IMPORT_LINK)

        verify(systemImpl, timeout(5000)).go(eq(ContentEntryImportLinkView.VIEW_NAME), eq(arguments), eq(context))
    }

    @Test
    fun givenUserIsAdmin_whenClicksOnAddContentIconAndSelectsImportFile_thenShouldOpenContentEntryEditActivity() {

        var personDaoSpy = spy(umAppRepository.personDao)

        var repo = spy(umAppRepository){
            on { personDao }.thenReturn(personDaoSpy)
        }

        runBlocking {
            var person = Person()
            person.admin = true
            whenever(personDaoSpy.findByUid(any())).thenReturn(person)

        }

        args[ARG_LIBRARIES_CONTENT] = ""
        args[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()
        var arguments = mutableMapOf<String, String>()
        arguments.putAll(args)
        arguments[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = rootEntry.contentEntryUid.toString()
        arguments[ARG_CONTENT_ENTRY_UID] = 0.toString()
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_IMPORT_FILE.toString()
        arguments[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()

        UmAccountManager.setActiveAccount(mockAccount!!, context)

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, adminAccount, systemImpl, repo)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).setEditButtonsVisibility(eq(arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG]!!.toInt()))

        presenter.handleClickAddContent(ContentEntryListView.CONTENT_IMPORT_FILE)

        verify(systemImpl, timeout(5000)).go(eq(ContentEntryEditView.VIEW_NAME), eq(arguments), eq(context))
    }


    @Test
    fun givenUserIsAdmin_whenClicksOnAddContentIconAndSelectsCreateContent_thenShouldOpenContentEntryEditActivity() {

        var personDaoSpy = spy(umAppRepository.personDao)

        var repo = spy(umAppRepository){
            on { personDao }.thenReturn(personDaoSpy)
        }

        runBlocking {
            var person = Person()
            person.admin = true
            whenever(personDaoSpy.findByUid(any())).thenReturn(person)

        }

        args[ARG_LIBRARIES_CONTENT] = ""
        args[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()
        var arguments = mutableMapOf<String, String>()
        arguments.putAll(args)
        arguments[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = rootEntry.contentEntryUid.toString()
        arguments[ARG_CONTENT_ENTRY_UID] = 0.toString()
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_CONTENT.toString()
        arguments[ContentEntryEditView.CONTENT_ENTRY_LEAF] = true.toString()

        UmAccountManager.setActiveAccount(mockAccount!!, context)

        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, adminAccount, systemImpl, repo)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).setEditButtonsVisibility(eq(arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG]!!.toInt()))

        presenter.handleClickAddContent(ContentEntryListView.CONTENT_CREATE_CONTENT)

        verify(systemImpl, timeout(5000)).go(eq(ContentEntryEditView.VIEW_NAME), eq(arguments), eq(context))
    }


    @Test
    fun givenUserIsAdmin_whenClicksOnEditContent_thenShouldOpenContentEntryEditActivity() {

        var personDaoSpy = spy(umAppRepository.personDao)

        var repo = spy(umAppRepository){
            on { personDao }.thenReturn(personDaoSpy)
        }

        runBlocking {
            var person = Person()
            person.admin = true
            whenever(personDaoSpy.findByUid(any())).thenReturn(person)

        }

        args[ARG_LIBRARIES_CONTENT] = ""
        args[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG] = (ContentEntryListView.EDIT_BUTTONS_ADD_CONTENT or ContentEntryListView.EDIT_BUTTONS_EDITOPTION).toString()
        var arguments = mutableMapOf<String, String>()
        arguments.putAll(args)
        arguments[ContentEntryImportLinkView.CONTENT_ENTRY_PARENT_UID] = rootEntry.contentEntryUid.toString()
        arguments[ARG_CONTENT_ENTRY_UID] = rootEntry.contentEntryUid.toString()
        arguments[ContentEntryEditView.CONTENT_TYPE] = ContentEntryListView.CONTENT_CREATE_FOLDER.toString()
        arguments[ContentEntryEditView.CONTENT_ENTRY_LEAF] = false.toString()


        var presenter = ContentEntryListPresenter(context, args, mockView, contentEntryDao, contentEntryRepoDao, adminAccount, systemImpl, repo)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).setEditButtonsVisibility(eq(arguments[ContentEntryListView.ARG_EDIT_BUTTONS_CONTROL_FLAG]!!.toInt()))

        presenter.handleClickEditButton()

        verify(systemImpl, timeout(5000)).go(eq(ContentEntryEditView.VIEW_NAME), eq(arguments), eq(context))

    }

}