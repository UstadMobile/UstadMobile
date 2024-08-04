package com.ustadmobile.core.viewmodel.clazz.redeem

import com.ustadmobile.core.domain.invite.ClazzRedeemUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on


data class InviteRedeemUiState(
    val onError: String? = null,
    val onRedeem: Boolean? = null
)

class ClazzInviteViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadViewModel(di, savedStateHandle, DEST_NAME) {

    private var _uiState = MutableStateFlow(InviteRedeemUiState())

    val uiState: Flow<InviteRedeemUiState> = _uiState.asStateFlow()

    private val clazzRedeemUseCase: ClazzRedeemUseCase by on(accountManager.activeEndpoint).instance()

    private val argInviteCode = savedStateHandle[ARG_INVITE_CODE]
        ?: throw IllegalArgumentException("no invite code")

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
            )
        }
        savedStateHandle[UstadView.ARG_RESULT_DEST_KEY]
    }


    fun processDecision(isAccepting:Boolean) {
        viewModelScope.launch {

            val result = clazzRedeemUseCase.invoke(argInviteCode,isAccepting)
            if (result.isCodeRedeem) {
                snackDispatcher.showSnackBar(Snack(result.message))
            } else {
                snackDispatcher.showSnackBar(Snack(result.message))
            }

            navController.navigate(
                ClazzListViewModel.DEST_NAME_HOME,
                args = emptyMap(),
                goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
            )
        }

    }

    companion object {

        const val DEST_NAME = "clazz_redeem"
        const val ARG_INVITE = "inviteCode"
    }
}