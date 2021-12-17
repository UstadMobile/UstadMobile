package com.ustadmobile.core.controller

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import com.ustadmobile.util.test.rules.CoroutineDispatcherRule
import com.ustadmobile.util.test.rules.bindPresenterCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import org.mockito.kotlin.*

class UstadBaseControllerTest {

    interface DummyView: UstadView

    class DummyViewPresenter(
        context: Any,
        arguments: Map<String, String>,
        view: DummyView,
        di: DI,
        sessionRequired: Boolean
    ): UstadBaseController<DummyView>(
        context, arguments, view, di, sessionRequired
    )

    private val userSession = UserSessionWithPersonAndEndpoint(
        UserSession().apply {
        usUid = 1
        usStatus = UserSession.STATUS_ACTIVE
    },
        Person().apply {
            firstNames = "test"
            lastName = "user"
            username = "testuser"
        },
        Endpoint("https://app.ustadmobile.com/")
    )

    private lateinit var di: DI

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var mockView: DummyView

    private lateinit var accountManager: UstadAccountManager

    private var numActiveSessions: Int = 1

    private lateinit var mockActiveSessionLive: DoorMutableLiveData<UserSessionWithPersonAndEndpoint?>

    @JvmField
    @Rule
    val coroutineScopeRule = CoroutineDispatcherRule()

    @Before
    fun setup() {
        systemImpl = mock {
            on { getAppConfigDefaultFirstDest(any()) }.thenReturn(ContentEntryList2View.VIEW_NAME)

            on{ getAppConfigBoolean(eq(AppConfig.KEY_ALLOW_SERVER_SELECTION), any()) }.thenReturn(true)
        }

        mockView = mock  { }

        mockActiveSessionLive = DoorMutableLiveData()

        accountManager = mock {
            onBlocking { activeSessionCount(any(), any()) }.thenAnswer {
                numActiveSessions
            }

            on { activeUserSessionLive }.thenReturn(mockActiveSessionLive)

        }



        di = DI {
            bind<UstadMobileSystemImpl>() with singleton {
                systemImpl
            }

            bind<UstadAccountManager>() with singleton {
                accountManager
            }

            bind<DoorLifecycleOwner>() with singleton {
                mock {
                    on { currentState }.thenReturn(UstadBaseController.RESUMED)
                }
            }

            bindPresenterCoroutineRule(coroutineScopeRule)
        }


    }

    @Test
    fun givenOneActiveSessionAndSessionIsRequired_whenSessionEnded_thenShouldNavigateToSelectSite() {
        mockActiveSessionLive.setVal(userSession)

        val presenter = DummyViewPresenter(Any(), mapOf(), mockView, di, true)
        presenter.onCreate(null)

        numActiveSessions = 0
        mockActiveSessionLive.setVal(null)

        verify(systemImpl, timeout(5000)).go(eq(SiteEnterLinkView.VIEW_NAME),
            any(), any(), argWhere {
                it.popUpToViewName == UstadView.ROOT_DEST && !it.popUpToInclusive
            })
    }

    @Test
    fun givenTwoActiveSessionAndSessionIsRequired_whenSessionIsEnded_thenShouldNavigateToAccountList() {
        mockActiveSessionLive.setVal(userSession)

        val presenter = DummyViewPresenter(Any(), mapOf(), mockView, di, true)
        presenter.onCreate(null)

        numActiveSessions = 1
        mockActiveSessionLive.setVal(null)

        verify(systemImpl, timeout(5000)).go(eq(AccountListView.VIEW_NAME),
            any(), any(), argWhere {
                it.popUpToViewName == UstadView.ROOT_DEST && !it.popUpToInclusive
            })
    }


}