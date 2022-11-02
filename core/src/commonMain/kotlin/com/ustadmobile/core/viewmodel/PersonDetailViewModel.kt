package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

data class PersonDetailUiState(

    //Keep this as a flow from the database so that it can be collected in a lifecycle-aware way
    // e.g. avoid observing this and running db queries when the screen is not visible
    val person: Flow<Person?> = flowOf(null),

    val changePasswordVisible: Flow<Boolean> = flowOf(false),

    val showCreateAccountVisible: Flow<Boolean> = flowOf(false),

    val chatVisible: Boolean = false,

)

class PersonDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    private val _uiState: MutableStateFlow<PersonDetailUiState>

    val uiState: StateFlow<PersonDetailUiState>
        get() = _uiState.asStateFlow()

    init {
        val db: UmAppDatabase by instance()

        val accountManager: UstadAccountManager by instance()

        val currentUserUid = accountManager.activeSession?.userSession?.usPersonUid ?: 0

        val entityUid: Long = savedStateHandle.get<String>(UstadView.ARG_ENTITY_UID)!!.toLong()

        val dbPersonFlow = db.personDao
            .findByUidAsFlow(entityUid)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val dbHasPermissionFlow = db.personDao.personHasPermissionFlow(currentUserUid,
            entityUid, Role.PERMISSION_RESET_PASSWORD)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val createAccountVisibleFlow : Flow<Boolean> = dbPersonFlow.combineTransform(
            dbHasPermissionFlow
        ) { person: Person?, hasResetPermission: Boolean ->
            person != null && person.username == null && hasResetPermission
        }

        val changePasswordVisible: Flow<Boolean> = dbPersonFlow.combineTransform(
            dbHasPermissionFlow
        ) { person: Person?, hasResetPermission: Boolean ->
            person?.username != null && hasResetPermission
        }

        _uiState = MutableStateFlow(
            PersonDetailUiState(
                person = dbPersonFlow,
                changePasswordVisible = changePasswordVisible,
                showCreateAccountVisible = createAccountVisibleFlow,
                chatVisible = currentUserUid != entityUid
            )
        )
    }

}