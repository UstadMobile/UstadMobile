
package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.UmAccount
import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountListPresenterTest {

    private lateinit var mockView: AccountListView

    private lateinit var context: Any

    private lateinit var accountManager: UstadAccountManager

    private val defaultTimeout:Long = 5000

    private lateinit var impl: UstadMobileSystemImpl

    private val  storedAccountsLiveData = DoorMutableLiveData<List<UmAccount>>()

    private val  activeAccountLiveData = DoorMutableLiveData<UmAccount>()

    @Before
    fun setup() {

        mockView = mock { }
        impl = mock{}

        accountManager = mock{
            on{storedAccountsLive}.thenReturn(storedAccountsLiveData)
            on{activeAccountLive}.thenReturn(activeAccountLiveData)
        }
        context = Any()
    }

    @Test
    fun givenStoreAccounts_whenLaunched_thenShouldShowAllAccounts(){
       val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        presenter.onCreate(null)

        storedAccountsLiveData.sendValue(listOf(UmAccount(1,"dummy",null,null)))
        nullableArgumentCaptor<DoorLiveData<List<UmAccount>>>().apply {
            verify(mockView, timeout(defaultTimeout).atLeastOnce()).accountListLiveData = capture()
            Assert.assertTrue("Account list was displayed to the view", firstValue != null)
        }
    }

    @Test
    fun givenActiveAccountExists_whenLaunched_thenShouldShowIt(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        presenter.onCreate(null)

        activeAccountLiveData.sendValue(UmAccount(1,"dummy",null,null))
        nullableArgumentCaptor<DoorLiveData<UmAccount>>().apply {
            verify(mockView, timeout(defaultTimeout).atLeastOnce()).activeAccountLive = capture()
            Assert.assertTrue("Active account was displayed to the view", lastValue != null)
        }
    }

    @Test
    fun givenAddAccountButton_whenClicked_thenShouldOpenGetStarted(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        presenter.onCreate(null)
        presenter.handleClickAddAccount()

        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            assertTrue("Get started was opened", GetStartedView.VIEW_NAME == firstValue)
        }
    }


    @Test
    fun givenDeleteAccountButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        val account = UmAccount(1,"dummy", null,null)
        presenter.onCreate(null)

        presenter.handleClickDeleteAccount(account)

        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }

    @Test
    fun givenLogoutButton_whenClicked_thenShouldRemoveAccountFromTheDevice(){
        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        val account = UmAccount(1,"dummy", null,null)
        presenter.onCreate(null)

        activeAccountLiveData.sendValue(account)

        presenter.handleClickLogout(account)
        argumentCaptor<UmAccount>{
            verify(accountManager).removeAccount(capture(), any())
            assertTrue("Expected account was removed from the device",
                    account == firstValue)
        }
    }


    @Test
    fun givenProfileButton_whenClicked_thenShouldGoToProfileView(){

        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        val account = UmAccount(1,"dummy", null,null)
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
        val presenter = AccountListPresenter(context, mapOf(), mockView, impl,
                accountManager)

        presenter.onCreate(null)

        presenter.handleClickAbout()

        argumentCaptor<String>{
            verify(impl).go(capture(), any(), any())
            assertTrue("About screen was opened", AboutView.VIEW_NAME == firstValue)
        }
    }
}