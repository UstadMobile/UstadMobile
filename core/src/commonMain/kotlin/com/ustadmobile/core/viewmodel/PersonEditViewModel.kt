package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.PersonConstants
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.flow.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.on

data class PersonEditUiState(

    val person: PersonWithPersonParentJoin? = null,

    val personPicture: PersonPicture? = null,

    val fieldsEnabled: Boolean = true,

    /**
     * This is set only when registering a minor
     */
    var approvalPersonParentJoin: PersonParentJoin? = null,

    var registrationMode: Int = 0,

    var usernameError: String? = null,

    var noMatchPasswordError: String? = null,

    var passwordError: String? = null,

    var emailError: String? = null,

    var confirmError: String? = null,

    var dateOfBirthError: String? = null,

    var parentContactError: String? = null,

    var firstNamesFieldError: String? = null,

    var lastNameFieldError: String? = null,

    var genderFieldError: String? = null,

    var firstNameError: String? = null,

    var lastNameError: String? = null,

)

class PersonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    val uiState: Flow<PersonEditUiState>

    init {


        val accountManager: UstadAccountManager by instance()

        val db: UmAppDatabase by on(accountManager.activeEndpoint).instance(tag = DoorTag.TAG_DB)

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