package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.ext.isDateOfBirthAMinor
import com.ustadmobile.core.view.SiteTermsDetailView.Companion.ARG_SHOW_ACCEPT_BUTTON
import com.ustadmobile.core.view.SiteTermsDetailView.Companion.ARG_USE_DISPLAY_LOCALE
import com.ustadmobile.core.view.UstadView.Companion.ARG_API_URL
import com.ustadmobile.core.view.UstadView.Companion.ARG_POPUPTO_ON_FINISH
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.datetime.Instant

data class RegisterAgeRedirectUiState(

    val dateOfBirth: Long = 0,

    val maxDate: Long = Long.MAX_VALUE,

    val dateOfBirthError: String? = null,

)

class RegisterAgeRedirectViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        RegisterAgeRedirectUiState(
            maxDate = systemTimeInMillis()
        )
    )

    val uiState: Flow<RegisterAgeRedirectUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = false,
                title = systemImpl.getString(MR.strings.register),
                userAccountIconVisible = false,
            )
        }
    }

    fun onSetDate(date: Long) {
        _uiState.update { prev ->
            prev.copy(
                dateOfBirth = date,
                dateOfBirthError = if(prev.dateOfBirth != date) null else prev.dateOfBirthError
            )
        }
    }

    fun onClickNext() {
        val date = _uiState.value.dateOfBirth
        if(date == 0L) {
            _uiState.update { prev ->
                prev.copy(dateOfBirthError = systemImpl.getString(MR.strings.field_required_prompt))
            }
            return
        }

        if(date > (systemTimeInMillis() - (24 * MS_PER_HOUR))) {
            _uiState.update { prev ->
                prev.copy(dateOfBirthError = systemImpl.getString(MR.strings.invalid))
            }
            return
        }

        val dateOfBirthInstant = Instant.fromEpochMilliseconds(date)

        val args = buildMap {
            putFromSavedStateIfPresent(ARG_API_URL)
            putFromSavedStateIfPresent(ARG_SHOW_ACCEPT_BUTTON)
            putFromSavedStateIfPresent(ARG_USE_DISPLAY_LOCALE)
            putFromSavedStateIfPresent(ARG_POPUPTO_ON_FINISH)
            putFromSavedStateIfPresent(ARG_NEXT)
            putFromSavedStateIfPresent(PersonEditViewModel.REGISTER_VIA_LINK)
            put(PersonEditViewModel.ARG_REGISTRATION_MODE,
                PersonEditViewModel.REGISTER_MODE_ENABLED.toString())
            put(PersonEditViewModel.ARG_DATE_OF_BIRTH, date.toString())
        }

        if(dateOfBirthInstant.isDateOfBirthAMinor()) {
            navController.navigate(PersonEditViewModel.DEST_NAME_REGISTER, args)
        }else {
            navController.navigate(SiteTermsDetailViewModel.DEST_NAME, args)
        }
    }





    companion object {

        const val DEST_NAME = "RegisterAgeRedirect"

    }

}
