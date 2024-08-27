package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class EditChildProfileUiState(
    val person: Person? = null,

    val dateOfBirthError: String? = null,

    val genderOptions: List<MessageIdOption2> = PersonConstants.GENDER_MESSAGE_IDS_AND_UNSET,

    val genderError: String? = null,

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    )

class EditChildProfileViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        EditChildProfileUiState()
    )
    private val addNewPersonUseCase: AddNewPersonUseCase by di.onActiveEndpoint().instance()

    val uiState: Flow<EditChildProfileUiState> = _uiState.asStateFlow()

    private val genderConfig: GenderConfig by instance()

    private fun EditChildProfileUiState.hasErrors(): Boolean {
        return dateOfBirthError != null ||
                firstNameError != null ||
                lastNameError != null ||
                genderError != null
    }

    init {
        _appUiState.update { prev ->
            prev.copy(

                title = systemImpl.getString(MR.strings.child_profile),
                hideBottomNavigation = true,
            )
        }
        _uiState.update { prev ->
            prev.copy(
                person = Person(),
                genderOptions = genderConfig.genderMessageIdsAndUnset,

                )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.done),
                    onClick = this@EditChildProfileViewModel::onClickDone
                )
            )
        }
        viewModelScope.launch {
            val selectedContentEntry = savedStateHandle.getJson(
                ARG_SELECTED_PERSON_ENTRY, Person.serializer()
            )

            val loadedEntity = loadEntity(
                serializer = Person.serializer(),
                makeDefault = {
                    val newUid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
                    Person(
                        personUid = newUid,
                        firstNames = selectedContentEntry?.firstNames,
                        lastName = selectedContentEntry?.lastName,
                        gender = selectedContentEntry?.gender?:0,
                        dateOfBirth = selectedContentEntry?.dateOfBirth?:0L,
                    )
                },
                onLoadFromDb = { null }, //Does not load from database - always JSON passed from ClazzEdit
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(person = it)
                    }
                }
            )

        }

    }

    fun onEntityChanged(entity: Person?) {
        _uiState.update { prev ->
            prev.copy(
                person = entity,
                genderError = updateErrorMessageOnChange(
                    prev.person?.gender,
                    entity?.gender, prev.genderError
                ),
                firstNameError = updateErrorMessageOnChange(
                    prev.person?.firstNames,
                    entity?.firstNames, prev.firstNameError
                ),
                lastNameError = updateErrorMessageOnChange(
                    prev.person?.lastName,
                    entity?.lastName, prev.lastNameError
                ),

                )
        }

        scheduleEntityCommitToSavedState(
            entity, serializer = Person.serializer(),
            commitDelay = 200
        )
    }

    fun onClickDone() {

        loadingState = LoadingUiState.INDETERMINATE
        val savePerson = _uiState.value.person ?: return

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)

        _uiState.update { prev ->
            prev.copy(
                firstNameError = if (savePerson.firstNames.isNullOrEmpty()) requiredFieldMessage else null,
                lastNameError = if (savePerson.lastName.isNullOrEmpty()) requiredFieldMessage else null,
                dateOfBirthError = if (savePerson.dateOfBirth == 0L) {
                    requiredFieldMessage
                } else {
                    null
                },
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }


        finishWithResult(_uiState.value.person)
    }


    companion object {

        const val DEST_NAME = "EditChildProfile"
        const val STATE_KEY_PERSON = "person"
        const val ARG_SELECTED_PERSON_ENTRY = "SelectedPersonEntry"

    }
}