package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.config.GenderConfig
import com.ustadmobile.core.impl.locale.entityconstants.PersonConstants
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel.Companion.ARG_PPJ_UID
import com.ustadmobile.core.viewmodel.person.toFirstAndLastNameExt
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.ext.shallowCopy
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

    val fullNameError: String? = null,

    val fullName: String? = null,

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

    private fun EditChildProfileUiState.hasErrors(): Boolean {
        return dateOfBirthError != null ||
                fullNameError != null ||
                genderError != null
    }

    init {
        _uiState.update { prev ->
            prev.copy(
                genderOptions = genderConfig.genderMessageIdsAndUnset,
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                hideAppBar =false,
                navigationVisible = false,
                userAccountIconVisible = false,
                title = systemImpl.getString(MR.strings.add_child_profile),
                hideBottomNavigation = true,
            )
        }


        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this@EditChildProfileViewModel::onClickSave
                )
            )
        }
        viewModelScope.launch {

            loadEntity(
                serializer = Person.serializer(),
                makeDefault = {
                    val newUid = activeDb.doorPrimaryKeyManager.nextIdAsync(Person.TABLE_ID)
                    Person(
                        personUid = newUid,
                        isPersonalAccount = true
                    )
                },
                onLoadFromDb = { null },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            person = it,
                            fullName = if (it?.firstNames == "") {
                                null
                            } else {
                                it?.fullName()
                            }
                        )
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
                fullNameError = updateErrorMessageOnChange(
                    prev.person?.firstNames,
                    entity?.firstNames, prev.fullNameError
                ),

                )
        }

        scheduleEntityCommitToSavedState(
            entity, serializer = Person.serializer(),
            commitDelay = 200
        )
    }
    fun onFullNameValueChange(fullName: String) {
        _uiState.update { prev ->
            prev.copy(
                fullName = fullName
            )
        }
    }
    fun onClickSave() {

        loadingState = LoadingUiState.INDETERMINATE
        val savePerson = _uiState.value.person ?: return

        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)

        _uiState.update { prev ->
            prev.copy(
                dateOfBirthError = if (savePerson.dateOfBirth == 0L) {
                    requiredFieldMessage
                } else {
                    null
                },
                genderError = if (savePerson.gender == GENDER_UNSET) requiredFieldMessage else null,
                fullNameError = if (_uiState.value.fullName.isNullOrEmpty()) requiredFieldMessage else null,
            )
        }

        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }
        val fullName = _uiState.value.fullName?.trim()
        val (firstName, lastName) = fullName.toFirstAndLastNameExt()
        onEntityChanged(
            _uiState.value.person?.shallowCopy {
                this.firstNames = firstName
                this.lastName = lastName
            }
        )
        val ppjUid = savedStateHandle[ARG_PPJ_UID]?.toLong()
        if (ppjUid!=null){
            viewModelScope.launch {
                val effectiveDb = activeRepo ?: activeDb

                effectiveDb.personDao().insertAsync(savePerson)

               val personParentJoin= PersonParentJoin(
                    ppjUid = ppjUid,
                    ppjMinorPersonUid = savePerson.personUid,
                    ppjParentPersonUid = accountManager.currentAccount.personUid,
                    ppjStatus = PersonParentJoin.STATUS_APPROVED,
                    ppjApprovalTiemstamp = systemTimeInMillis()
                )
                accountManager.addSession(savePerson, accountManager.activeLearningSpace.url, null)

                effectiveDb.personParentJoinDao().upsertAsync(personParentJoin)
                navController.navigateToViewUri(
                    ContentEntryListViewModel.DEST_NAME_HOME.appendSelectedAccount(
                        accountManager.currentAccount.personUid,
                        LearningSpace(accountManager.activeLearningSpace.url)
                    ),
                    UstadMobileSystemCommon.UstadGoOptions(clearStack = true)
                )
            }
            return
        }
        finishWithResult(_uiState.value.person)
    }


    companion object {

        const val DEST_NAME = "EditChildProfile"

    }
}