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
import kotlinx.coroutines.launch
import org.kodein.di.on

data class PersonEditUiState(

    val person: PersonWithAccount? = null,

    val personPicture: PersonPicture? = null,

    val fieldsEnabled: Boolean = true,

    /**
     * This is set only when registering a minor
     */
    val approvalPersonParentJoin: PersonParentJoin? = null,

    val registrationMode: Int = 0,

    val usernameError: String? = null,

    val passwordConfirmedError: String? = null,

    val passwordError: String? = null,

    val emailError: String? = null,

    val confirmError: String? = null,

    val dateOfBirthError: String? = null,

    val parentContactError: String? = null,

    val genderError: String? = null,

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    val usernameVisible: Boolean = false,

    val passwordVisible: Boolean = false,

    val parentalEmailVisible: Boolean = false,
)

class PersonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<Person>(di, savedStateHandle) {

    private val _uiState: MutableStateFlow<PersonEditUiState> = MutableStateFlow(PersonEditUiState())

    val uiState: Flow<PersonEditUiState> = _uiState.asStateFlow()

    init {


        val accountManager: UstadAccountManager by instance()

        val db: UmAppDatabase by on(accountManager.activeEndpoint).instance(tag = DoorTag.TAG_DB)

        val currentUserUid = accountManager.activeSession?.userSession?.usPersonUid ?: 0

        val entityUid: Long = savedStateHandle.get(UstadView.ARG_ENTITY_UID)!!.toLong()

        viewModelScope.launch {
            val person = db.personDao.findPersonAccountByUid(entityUid)
            _uiState.update { prev ->
                prev.copy(person = person)
            }
        }
    }


    fun onEntityChanged(entity: PersonWithAccount?) {
        _uiState.update { prev ->
            prev.copy(person = entity)
        }
    }

    fun getGender(gender: Int = 0): String {
        val genderId: Int? = PersonConstants.GENDER_MESSAGE_ID_MAP[gender]
        val systemImpl: UstadMobileSystemImpl = di.direct.instance()
        return systemImpl.getString(genderId ?: 0)
    }
}