package com.ustadmobile.core.viewmodel.clazz.joinwithcode

import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.RequestEnrolmentUseCase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.AlreadyEnroledInClassException
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.AlreadyHasPendingRequestException
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel

data class JoinWithCodeUiState(

    val codeError: String? = null,

    val code: String = "",

    val fieldsEnabled: Boolean = true,

)

class JoinWithCodeViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadViewModel(di, savedStateHandle, DEST_NAME) {


    private val _uiState = MutableStateFlow(JoinWithCodeUiState())

    val uiState: Flow<JoinWithCodeUiState> = _uiState.asStateFlow()

    private val requestEnrolmentUseCase: RequestEnrolmentUseCase by di.onActiveEndpoint().instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.join_existing_course)
            )
        }
    }


    fun onCodeValueChange(codeVal: String) {
        _uiState.update { prev ->
            prev.copy(
                code = codeVal,
                codeError = null,
            )
        }
    }

    fun onClickJoin() {
        launchWithLoadingIndicator(
            onSetFieldsEnabled = {
                _uiState.update { prev ->
                    prev.copy(fieldsEnabled = it)
                }
            }
        ) {
            try {
                requestEnrolmentUseCase(
                    clazzCode = _uiState.value.code,
                    person = accountManager.currentUserSession.person,
                    roleId = ClazzEnrolment.ROLE_STUDENT
                )

                snackDispatcher.showSnackBar(
                    Snack(systemImpl.getString(MR.strings.request_submitted))
                )

                if(expectedResultDest != null) {
                    finishWithResult(null)
                }else {
                    navController.navigate(
                        ClazzListViewModel.DEST_NAME_HOME,
                        args = emptyMap(),
                        goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                    )
                }
            }catch(e: Throwable) {
                val errorMessage = when(e) {
                    is IllegalArgumentException -> MR.strings.invalid_course_code
                    is AlreadyHasPendingRequestException -> MR.strings.request_to_enrol_already_pending
                    is AlreadyEnroledInClassException -> MR.strings.you_are_already_in_class
                    else -> MR.strings.error
                }

                _uiState.update { prev ->
                    prev.copy(
                        codeError = systemImpl.getString(errorMessage) + (e.message?.let { " :$it" } ?: "")
                    )
                }
            }
        }
    }

    companion object {

        const val DEST_NAME = "JoinWithCode"

    }

}
