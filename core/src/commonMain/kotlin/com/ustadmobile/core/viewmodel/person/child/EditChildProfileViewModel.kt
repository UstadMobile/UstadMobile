package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    val uiState: Flow<EditChildProfileUiState> = _uiState.asStateFlow()

    private val genderConfig: GenderConfig by instance()


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
        accountManager.currentUserSession.person.personUid
    }


    companion object {

        const val DEST_NAME = "EditChildProfile"

    }
}