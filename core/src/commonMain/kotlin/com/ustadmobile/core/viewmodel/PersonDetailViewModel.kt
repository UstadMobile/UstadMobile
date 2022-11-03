package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.instance

data class PersonDetailUiState(

    //Keep this as a flow from the database so that it can be collected in a lifecycle-aware way
    // e.g. avoid observing this and running db queries when the screen is not visible
    val person: PersonWithPersonParentJoin? = null,

    val changePasswordVisible: Boolean = false,

    val showCreateAccountVisible: Boolean = false,

    val chatVisible: Boolean = false,

    )

class PersonDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    val uiState: Flow<PersonDetailUiState>

    init {
        val db: UmAppDatabase by instance()

        val accountManager: UstadAccountManager by instance()

        val currentUserUid = accountManager.activeSession?.userSession?.usPersonUid ?: 0

        val entityUid: Long = savedStateHandle.get<String>(UstadView.ARG_ENTITY_UID)!!.toLong()

        val dbPersonFlow = db.personDao
            .findByUidWithDisplayDetailsFlow(entityUid, currentUserUid)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        val dbHasPermissionFlow = db.personDao.personHasPermissionFlow(currentUserUid,
            entityUid, Role.PERMISSION_RESET_PASSWORD)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        uiState = dbPersonFlow.combine(dbHasPermissionFlow) { person, personHasPermission ->
            PersonDetailUiState(
                person = person,
                changePasswordVisible = personHasPermission && person?.username != null,
                showCreateAccountVisible = personHasPermission && person != null && person.username == null,
                chatVisible = person != null && person.personUid != currentUserUid
            )
        }
    }

}

