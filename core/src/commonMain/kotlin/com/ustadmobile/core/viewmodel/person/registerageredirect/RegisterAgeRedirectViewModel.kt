package com.ustadmobile.core.viewmodel.person.registerageredirect

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.ext.isDateOfBirthAMinor
import com.ustadmobile.core.view.SiteTermsDetailView.Companion.ARG_SHOW_ACCEPT_BUTTON
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailViewModel
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.core.viewmodel.signup.SignUpViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.datetime.Instant

data class RegisterAgeRedirectUiState(

    val dateOfBirth: Long = 0,

    val maxDate: Long = Long.MAX_VALUE,

    val dateOfBirthError: String? = null,

    )

/**
 * This screen implements the "age neutral" screen requirement as per COPPA and Google Play policies.
 * Any app that can appeal to children must have a screen where the age is requested in a neutral
 * manner.
 *
 * If the user is under 13, we must request a parental consent contact and minimize the fields
 * collected.
 *
 * If the user is over 13, they can be taken to consent to the terms of service directly. If they
 * are under 13, then we take them to PersonEdit to put in their details and provide a parental
 * consent contact.
 */
class RegisterAgeRedirectViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

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
                title = systemImpl.getString(MR.strings.your_date_of_birth),
                userAccountIconVisible = false,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.next),
                    onClick = this::onClickNext
                )
            )
        }
    }

    fun onSetDate(date: Long) {
        _uiState.update { prev ->
            prev.copy(
                dateOfBirth = date,
                dateOfBirthError = if (prev.dateOfBirth != date) null else prev.dateOfBirthError
            )
        }
    }

    fun onClickNext() {
        val date = _uiState.value.dateOfBirth
        if (date == 0L) {
            _uiState.update { prev ->
                prev.copy(dateOfBirthError = systemImpl.getString(MR.strings.field_required_prompt))
            }
            return
        }

        if (date > (systemTimeInMillis() - (24 * MS_PER_HOUR))) {
            _uiState.update { prev ->
                prev.copy(dateOfBirthError = systemImpl.getString(MR.strings.invalid))
            }
            return
        }

        val dateOfBirthInstant = Instant.fromEpochMilliseconds(date)
        val isMinor = dateOfBirthInstant.isDateOfBirthAMinor()

        val args = buildMap {
            putFromSavedStateIfPresent(SignUpViewModel.REGISTRATION_ARGS_TO_PASS)
            put(ARG_SHOW_ACCEPT_BUTTON,true.toString())


            put(PersonEditViewModel.ARG_DATE_OF_BIRTH, date.toString())
        }

        if (isMinor) {
            //not decided where to go
            //navController.navigate(PersonEditViewModel.DEST_NAME_REGISTER, args)
        } else {
            navController.navigate(SiteTermsDetailViewModel.DEST_NAME, args)
        }


    }


    companion object {

        const val DEST_NAME = "RegisterAgeRedirect"

    }

}
