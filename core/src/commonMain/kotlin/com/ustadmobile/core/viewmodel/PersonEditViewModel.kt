package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithPersonParentJoin
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.util.MessageIdOption

data class PersonEditUiState(

    val person: PersonWithPersonParentJoin? = null,

    val genderList: List<MessageIdOption> = listOf(),

    )

class PersonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    val uiState: Flow<PersonEditUiState>

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
            PersonEditUiState(
                person = person,
            )
        }
    }

    fun getGender(gender: Int = 0): String {
        val genderId: Int? = PersonConstants.GENDER_MESSAGE_ID_MAP[gender]
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        return systemImpl.getString(genderId ?: 0)
    }
}