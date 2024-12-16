package com.ustadmobile.core.viewmodel.person.registerageredirect

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import org.junit.Test
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.test.assertEquals

class RegisterAgeRedirectViewModelTest : AbstractMainDispatcherTest(){

    @Test
    fun givenDateOfBirthNotSet_whenOnClickNextCalled_thenShouldShowError(){
        testViewModel<RegisterAgeRedirectViewModel> {
            viewModelFactory {
                RegisterAgeRedirectViewModel(di, savedStateHandle)
            }

            viewModel.onClickNext()
            viewModel.uiState.assertItemReceived {
                it.dateOfBirthError == systemImpl.getString(MR.strings.field_required_prompt)
            }
        }
    }

    @Test
    fun givenDateOfBirthTodayOrInFuture_whenOnClickNextCalled_thenShouldShowError() {
        testViewModel<RegisterAgeRedirectViewModel> {
            viewModelFactory {
                RegisterAgeRedirectViewModel(di, savedStateHandle)
            }

            viewModel.onSetDate(systemTimeInMillis())
            viewModel.onClickNext()

            viewModel.uiState.assertItemReceived {
                it.dateOfBirthError == systemImpl.getString(MR.strings.invalid)
            }
        }
    }
// commented by nikunj on sept 5 , 2024 because during registration if
    //person is minor we haven't decided what should be the exact flow
//    @Test
//    fun givenDateOfBirthIsAMinor_whenOnClickNextCalled_thenShouldGoToPersonEdit() {
//        testViewModel<RegisterAgeRedirectViewModel> {
//            viewModelFactory {
//                RegisterAgeRedirectViewModel(di, savedStateHandle)
//            }
//
//            val minorDateOfBirth = Clock.System.now().minus(
//                1L, DateTimeUnit.YEAR, TimeZone.UTC)
//
//            viewModel.onSetDate(minorDateOfBirth.toEpochMilliseconds())
//            viewModel.onClickNext()
//
//            viewModel.navCommandFlow.test {
//                val navCommand = awaitItem() as NavigateNavCommand
//                assertEquals(PersonEditViewModel.DEST_NAME_REGISTER, navCommand.viewName)
//            }
//        }
//    }

    @Test
    fun givenDateOfBirthIsNotAMinor_whenOnClickNextCalled_thenShouldGoToTerms() {
        testViewModel<RegisterAgeRedirectViewModel> {
            viewModelFactory {
                RegisterAgeRedirectViewModel(di, savedStateHandle)
            }

            val minorDateOfBirth = Clock.System.now().minus(
                14L, DateTimeUnit.YEAR, TimeZone.UTC)

            viewModel.onSetDate(minorDateOfBirth.toEpochMilliseconds())
            viewModel.onClickNext()

            viewModel.navCommandFlow.test {
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals(SiteTermsDetailViewModel.DEST_NAME, navCommand.viewName)
            }
        }
    }

}