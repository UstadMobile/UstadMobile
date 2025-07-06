package com.ustadmobile.core.viewmodel.clazz.inviteredeem

import com.ustadmobile.core.domain.invite.ClazzInviteRedeemUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.composites.ClazzInviteAndClazz
import com.ustadmobile.lib.db.entities.ClazzInvite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import com.ustadmobile.core.util.ext.onActiveLearningSpace
import com.ustadmobile.core.util.ext.stringResourceOrMessage

data class ClazzInviteRedeemUiState(
    val clazzInvite: ClazzInviteAndClazz? = null,
    val enabled: Boolean = false,
    val errorText: String? = null,
) {

    val showButtons: Boolean
        get() = clazzInvite?.clazzInvite?.inviteStatus == ClazzInvite.STATUS_PENDING

    val buttonsEnabled: Boolean
        get() = enabled && clazzInvite?.clazzInvite?.inviteStatus == ClazzInvite.STATUS_PENDING

    val inviteUsed: Boolean
        get() = clazzInvite?.clazzInvite != null && clazzInvite.clazzInvite?.inviteStatus != ClazzInvite.STATUS_PENDING

}

class ClazzInviteRedeemViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzInviteRedeemUiState())

    val uiState: Flow<ClazzInviteRedeemUiState> = _uiState.asStateFlow()

    private val clazzInviteRedeemUseCase: ClazzInviteRedeemUseCase by
        di.onActiveLearningSpace().instance()

    private val argInviteCode = savedStateHandle[ARG_INVITE_CODE]
        ?: throw IllegalArgumentException("no invite code")

    init {
        ifLoggedInElseNavigateToLoginWithNextDestSet(
            requireAdultAccount = false,
            args = mapOf(ARG_INVITE_CODE to argInviteCode)
        ) {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                )
            }

            viewModelScope.launch {
                _uiState.whenSubscribed {
                    activeRepoWithFallback.clazzInviteDao().findClazzInviteEntityForInviteTokenAsFlow(
                        argInviteCode
                    ).collect { inviteAndClazz ->
                        _appUiState.update {
                            it.copy(
                                loadingState = if(inviteAndClazz?.clazzInvite != null) {
                                    LoadingUiState.NOT_LOADING
                                }else {
                                    LoadingUiState.INDETERMINATE
                                },
                            )
                        }

                        _uiState.update {
                            it.copy(
                                clazzInvite = inviteAndClazz,
                                enabled = inviteAndClazz?.clazzInvite != null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun processDecision(isAccepting: Boolean) {
        viewModelScope.takeIf { _uiState.value.enabled }?.launch {
            _uiState.update { prev -> prev.copy(enabled = false) }

            try {
                val result = clazzInviteRedeemUseCase(
                    inviteCode = argInviteCode,
                    isAccepting = isAccepting,
                    personUid = accountManager.currentAccount.personUid
                )

                snackDispatcher.showSnackBar(Snack(result.message))


                navController.navigate(
                    viewName = ClazzListViewModel.DEST_NAME_HOME,
                    args = emptyMap(),
                    goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                )
            }catch(e: Throwable) {
                _uiState.update { prev ->
                    prev.copy(
                        errorText = e.stringResourceOrMessage(systemImpl),
                        enabled = true,
                    )
                }
            }
        }

    }

    companion object {

        const val DEST_NAME = "ClazzInviteRedeem"

    }
}