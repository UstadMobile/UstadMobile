
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.AboutView
import com.ustadmobile.core.view.AccountListView
import com.ustadmobile.core.view.GetStartedView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

class AccountListPresenterTest {

    private lateinit var mockView: AccountListView

    private lateinit var context: Any

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout:Long = 5000

    private lateinit var impl: UstadMobileSystemImpl

    private val  accountListLive = DoorMutableLiveData<List<UmAccount>>()

    private val  activeAccountLive = DoorMutableLiveData<UmAccount>()

    private lateinit var mockedAccountListObserver:DoorObserver<List<UmAccount>>

    private lateinit var mockedAccountObserver:DoorObserver<UmAccount>

    private val accountList = listOf(UmAccount(1,"dummy",null,""))

    private lateinit var di: DI

    @Before
    fun setup() {

        mockView = mock { }
        impl = mock{}

        accountManager = mock{
            on{storedAccountsLive}.thenReturn(accountListLive)
            on{activeAccountLive}.thenReturn(activeAccountLive)
        }
        context = Any()

        mockedAccountListObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList }
        }

        mockedAccountObserver = mock{
            on{ onChanged(any()) }.thenAnswer{ accountList[0] }
        }

        di = DI {
            bind<UstadMobileSystemImpl>() with singleton { impl }
            bind<UstadAccountManager>() with singleton { accountManager }
        }
    }

    @Test
    fun givenStoreAccounts_whenAppLaunched_thenShouldShowAllAccounts(){
       val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        presenter.onCreate(null)
        accountListLive.observeForever(mockedAccountListObserver)
        accountListLive.sendValue(accountList)
        argumentCaptor<List<UmAccount>>{
            verify(mockedAccountListObserver, timeout(defaultTimeout).atLeastOnce()).onChanged(capture())
            assertTrue("Account list was displayed", accountList.containsAll(lastValue))
        }
    }

    @Test
    fun givenActiveAccountExists_whenAppLaunched_thenShouldShowIt(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        presenter.onCreate(null)

        activeAccountLive.observeForever(mockedAccountObserver)
        activeAccountLive.sendValue(accountList[0])
        argumentCaptor<UmAccount>{
            verify(mockedAccountObserver, timeout(defaultTimeout).atLeastOnce()).onChanged(capture())
            assertEquals("Active account was displayed", accountList[0], lastValue)
        }
    }

    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenGetStarted(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        presenter.onCreate(null)
        presenter.handleClickAddAccount()

        argumentCaptor<String>{
            verify(impl).go(capture(), any())
            assertTrue("Get started was opened", GetStartedView.VIEW_NAME == firstValue)
        }
    }


    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        presenter.handleClickDeleteAccount(account)

        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }

    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        activeAccountLive.sendValue(account)

        presenter.handleClickLogout(account)
        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }


    @Test
    fun givenAccountList_whenAccountIsClicked_shouldBeActive(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        activeAccountLive.sendValue(account)

        presenter.handleClickAccount(account)
        argumentCaptor<UmAccount>{
            verify(accountManager).activeAccount = capture()
            assertTrue("Expected account was set active",
                    account == firstValue)
        }
    }


    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){

        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        val account = UmAccount(1,"dummy", null,"")
        presenter.onCreate(null)

        presenter.handleClickProfile(account.personUid)

        argumentCaptor<Map<String,String>>{
            verify(impl).go(eq(PersonDetailView.VIEW_NAME), capture(), any())
            assertTrue("Person details view was opened with right person id",
                    account.personUid == firstValue[ARG_ENTITY_UID]?.toLong())
        }
    }

    @Test
    fun givenAboutButton_whenClicked_thenShouldGoToAboutView(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, di)

        presenter.onCreate(null)

        presenter.handleClickAbout()

        argumentCaptor<String>{
            verify(impl).go(capture(),any())
            assertTrue("About screen was opened", AboutView.VIEW_NAME == firstValue)
        }
    }
}