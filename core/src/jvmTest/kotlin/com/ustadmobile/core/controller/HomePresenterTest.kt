package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryListView.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class HomePresenterTest {

    private lateinit var context: DoorLifecycleOwner

    private lateinit var mockView: HomeView

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var presenter: HomePresenter

    private lateinit var systemImpl: UstadMobileSystemImpl

    @Before
    fun setup(){
        context = mock{
            on{
                currentState
            }.thenReturn(DoorLifecycleObserver.STARTED)
        }

        systemImpl = mock {
            on {
                getAppConfigString(AppConfig.KEY_SHOW_DOWNLOAD_ALL_BTN, null, context)
            }.thenReturn("false")

            on{
                getAllUiLanguage(context)
            }.thenReturn(mapOf("en" to "English"))

            on{
                getDisplayedLocale(context)
            }.thenReturn("en")
        }

        mockView =  mock {
            on { runOnUiThread(ArgumentMatchers.any()) }.doAnswer { invocation ->
                Thread(invocation.getArgument<Any>(0) as Runnable).start()
                Unit
            }
        }

        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()
        presenter = HomePresenter(context, mapOf(), mockView, umAppDatabase.personDao,systemImpl)
    }

    @After
    fun tearDown() {
        umAppDatabase.clearAllTables()
    }

    @Test
    fun givenApplicationLaunched_WhenAdminLoggedIn_shouldShowBothContentsAndReportsNavsWithAllFilters(){
        val person = Person("username","Name1","name2")
        person.admin = true
        val personUid = umAppDatabase.personDao.insert(person)
        val umAccount = UmAccount(personUid, "", "", "")
        UmAccountManager.setActiveAccount(umAccount, context)
        presenter.onCreate(null)
        argumentCaptor<List<Pair<Int, String>>>().apply {
            verify(mockView, timeout(5000)).setOptions(capture())
            assertEquals("Two navs were displayed",2, firstValue.size)
            assertTrue("All filters were included",
                     firstValue[0].second.contains(ARG_LIBRARIES_CONTENT)
                             && firstValue[0].second.contains(ARG_DOWNLOADED_CONTENT)
                             && firstValue[0].second.contains(ARG_RECYCLED_CONTENT))
        }
    }

    @Test
    fun givenApplicationLaunched_WhenNoAdminLoggedIn_shouldShowOnlyContentsNavWithLibrariesAndDownloadedFilters(){
        presenter.onCreate(null)
        argumentCaptor<List<Pair<Int, String>>>().apply {
            verify(mockView, timeout(5000)).setOptions(capture())
            assertEquals("One nav was displayed",1, firstValue.size)
            assertTrue("Both libraries and downloaded filters were included",
                    firstValue[0].second.contains(ARG_LIBRARIES_CONTENT)
                            && firstValue[0].second.contains(ARG_DOWNLOADED_CONTENT))
        }
    }
}