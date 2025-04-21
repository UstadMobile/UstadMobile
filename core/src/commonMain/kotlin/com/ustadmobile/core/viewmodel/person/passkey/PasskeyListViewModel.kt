package com.ustadmobile.core.viewmodel.person.passkey

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.credentials.UserPasskeyChallenge
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.PersonPasskey
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

data class PasskeyListUiState(
    val passkeys: ListPagingSourceFactory<PersonPasskey> = { EmptyPagingSource() },
    val showRevokePasskeyDialog : Boolean = false,
    val personPasskeyUid : Long = 0
)


class PasskeyListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
) : UstadListViewModel<PasskeyListUiState>(
    di, savedStateHandle, PasskeyListUiState(), DEST_NAME,
) {

    private val apiUrlConfig: SystemUrlConfig by instance()

    val repo: UmAppDatabase =
        di.on(LearningSpace(apiUrlConfig.systemBaseUrl)).direct.instance<UmAppDataLayer>()
            .requireRepository()
    private val passkeysPagingSource: () -> PagingSource<Int, PersonPasskey> = {
        repo.personPasskeyDao().getAllActivePasskeysPaging(accountManager.currentAccount.personUid)
    }
    private val impl: UstadMobileSystemImpl by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = impl.getString(MR.strings.passkeys),
                userAccountIconVisible = false,
                navigationVisible = true,
            )
        }


        viewModelScope.launch {
            _uiState.whenSubscribed {
                _uiState.update { prev ->
                    prev.copy(
                        passkeys = passkeysPagingSource
                    )
                }

            }
        }

    }
   fun onDismissRevokePasskeyDialog(){
       _uiState.update { prev ->
           prev.copy(
               showRevokePasskeyDialog = false
           )
       }
   }
    fun findUserChallenge(userChallenge: String): UserPasskeyChallenge {
        val challenge = userChallenge.decodeBase64Bytes().decodeToString()
        return json.decodeFromString(UserPasskeyChallenge.serializer(), challenge)
    }

    fun revokePasskey() {
        viewModelScope.launch {
            repo.personPasskeyDao().revokePersonPasskey(_uiState.value.personPasskeyUid)
            onDismissRevokePasskeyDialog()
        }
    }
    fun onClickRevokePasskey(personPasskeyUid: Long){
        _uiState.update { prev ->
            prev.copy(
                showRevokePasskeyDialog = true,
                personPasskeyUid = personPasskeyUid
            )
        }
    }


    companion object {

        const val DEST_NAME = "PasskeyList"


    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        //Do nothing
    }

}
