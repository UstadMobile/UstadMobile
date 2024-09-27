package com.ustadmobile.core.viewmodel.accountlist

import app.cash.turbine.test
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.lib.db.entities.Site
import org.junit.Test
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.kodein.di.singleton
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class AccountListViewModelTest : AbstractMainDispatcherTest() {


    @Test
    fun whenClickAddAccount_presetLearningSpaceIsNonNullAndRegistrationNotAllowed_shouldNavigateToLogin() =
        testViewModel<AccountListViewModel> {
            val repo: UmAppDatabase =
                di.on(LearningSpace("http://app.ustadmobile.com/")).direct.instance<UmAppDataLayer>()
                    .requireRepository()
            val newSite = Site().apply {
                registrationAllowed = false
            }

            repo.siteDao().insertAsync(newSite)
            viewModelFactory {
                AccountListViewModel(di, savedStateHandle)

            }
            extendDi {

                bind<SystemUrlConfig>(overrides = true) with singleton {
                    SystemUrlConfig(
                        "http://app.ustadmobile.com/", "app.ustadmobile.com",
                        presetLearningSpaceUrl = "http://app.ustadmobile.com/"
                    )
                }


            }


            viewModel.onClickAddAccount()

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    LoginViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for Login screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }

    @Test
    fun whenClickAddAccount_presetLearningSpaceIsNonNullAndRegistrationAllowed_shouldNavigateToAddAccountSelect() =
        testViewModel<AccountListViewModel> {
            val repo: UmAppDatabase =
                di.on(LearningSpace("http://app.ustadmobile.com/")).direct.instance<UmAppDataLayer>()
                    .requireRepository()
            val newSite = Site().apply {
                registrationAllowed = true
            }

            repo.siteDao().insertAsync(newSite)
            viewModelFactory {
                AccountListViewModel(di, savedStateHandle)

            }


            viewModel.onClickAddAccount()

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    AddAccountSelectNewOrExistingViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for AddAccountSelectNewOrExisting screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }



}