package com.ustadmobile.core.viewmodel

import app.cash.turbine.test
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import org.junit.Test
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class AddAccountSelectNewOrExistingViewModelTest : AbstractMainDispatcherTest() {


    @Test
    fun whenClickExistingUser_presetLearningSpaceIsNullAndPersonalAccountsLearningSpaceUrlNonNull_shouldNavigateToAddAccountSelectNewUserType() =
        testViewModel<AddAccountSelectNewOrExistingViewModel> {

            viewModelFactory {
                AddAccountSelectNewOrExistingViewModel(di, savedStateHandle)

            }
            extendDi {
                bind<SystemUrlConfig>(overrides = true) with singleton {
                    SystemUrlConfig(
                        "http://app.ustadmobile.com/", "app.ustadmobile.com",
                        newPersonalAccountsLearningSpaceUrl = "http://app.ustadmobile.com/"
                    )
                }

            }


            viewModel.navigateUser(false)

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    AddAccountSelectNewOrExistingUserTypeViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for AddAccountSelectNewUserType screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }

    @Test
    fun whenClickNewUser_presetLearningSpaceIsNullAndPersonalAccountsLearningSpaceUrlNonNull_shouldNavigateToAddAccountSelectNewUserType() =
        testViewModel<AddAccountSelectNewOrExistingViewModel> {

            viewModelFactory {
                AddAccountSelectNewOrExistingViewModel(di, savedStateHandle)

            }
            extendDi {
                bind<SystemUrlConfig>(overrides = true) with singleton {
                    SystemUrlConfig(
                        "http://app.ustadmobile.com/", "app.ustadmobile.com",
                        newPersonalAccountsLearningSpaceUrl = "http://app.ustadmobile.com/"
                    )
                }

            }


            viewModel.navigateUser(true)

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    AddAccountSelectNewOrExistingUserTypeViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for AddAccountSelectNewUserType screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }


    @Test
    fun whenClickNewUser_presetLearningSpaceAndPersonalAccountsLearningSpaceUrlIsNull_shouldNavigateToLearningSpaceList() =
        testViewModel<AddAccountSelectNewOrExistingViewModel> {

            viewModelFactory {
                AddAccountSelectNewOrExistingViewModel(di, savedStateHandle)

            }
            extendDi {
                bind<SystemUrlConfig>(overrides = true) with singleton {
                    SystemUrlConfig(
                        "http://app.ustadmobile.com/", "app.ustadmobile.com",
                    )
                }

            }


            viewModel.navigateUser(true)

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    LearningSpaceListViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for LearningSpaceListViewModel screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }


    @Test
    fun whenClickExistingUser_presetLearningSpaceAndPersonalAccountsLearningSpaceUrlIsNull_shouldNavigateToLearningSpaceList()  =
        testViewModel<AddAccountSelectNewOrExistingViewModel> {

            viewModelFactory {
                AddAccountSelectNewOrExistingViewModel(di, savedStateHandle)

            }
            extendDi {
                bind<SystemUrlConfig>(overrides = true) with singleton {
                    SystemUrlConfig(
                        "http://app.ustadmobile.com/", "app.ustadmobile.com",
                    )
                }

            }


            viewModel.navigateUser(false)

            viewModel.navCommandFlow.test(timeout = 5.seconds) {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(
                    LearningSpaceListViewModel.DEST_NAME, navCommand.viewName,
                    "Navigated to wait for LearningSpaceList screen"
                )

                cancelAndIgnoreRemainingEvents()
            }

        }
}