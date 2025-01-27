package com.ustadmobile.core.viewmodel.clazz.inviteviaContact

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.invite.ResendInviteUseCase
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactViewModel.InviteResult
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzInvite
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.instance


data class PendingInviteUiState(
    val pendingInviteList: ListPagingSourceFactory<ClazzInvite> = { EmptyPagingSource() },
)

class PendingInviteViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<PendingInviteUiState>(
    di, savedStateHandle, PendingInviteUiState(), PendingInviteViewModel.DEST_NAME
) {
    private val clazzUid = savedStateHandle[PendingInviteViewModel.ARG_CLAZZ_UID]?.toLong() ?: 0L
    private val resendInviteUseCase: ResendInviteUseCase by di.onActiveEndpoint().instance()


    private val pagingSource: () -> PagingSource<Int, ClazzInvite> = {
        activeRepoWithFallback.clazzInviteDao().findPendingInviteByPersonUid(
            ciPersonUid = accountManager.currentUserSession.userSession.usPersonUid,
            clazzUid = clazzUid,
            currentTime = systemTimeInMillis()

        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.pending_invites),
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {

                _uiState.update { prev ->
                    prev.copy(
                        pendingInviteList = pagingSource

                    )
                }
            }
        }
    }

    fun onClickRevokeInvite(contact: String) {
        viewModelScope.launch {
            activeRepoWithFallback.clazzInviteDao().updateClazzInviteToRevokeInvite(contact)
        }
    }

    fun onClickResendInvite(contact: String) {
        viewModelScope.launch {
            val result = resendInviteUseCase.invoke(
                contact,
                accountManager.currentUserSession.person.personUid
            )

            val invitation = Json.decodeFromString<InviteResult>(result)

            snackDispatcher.showSnackBar(Snack(invitation.inviteSent))
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        //not in use here
    }

    override fun onClickAdd() {
        //not in use here
    }


    companion object {
        const val DEST_NAME = "pending-invite"
        const val ARG_CLAZZ_UID = "clazz_uid"
    }


}



