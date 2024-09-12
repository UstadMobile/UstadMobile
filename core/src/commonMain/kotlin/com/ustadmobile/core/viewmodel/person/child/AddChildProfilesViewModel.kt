package com.ustadmobile.core.viewmodel.person.child

import com.ustadmobile.core.MR
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.account.UserSessionWithPersonAndLearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.usersession.StartUserSessionUseCase
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.replaceOrAppend
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance


data class AddChildProfilesUiState(
    val onAddChildProfile: String? = null,
    val childProfiles: List<Person> = emptyList(),
    val personParenJoinList: List<PersonParentJoin> = emptyList(),
    val showProfileSelectionDialog: Boolean = false,
    val parent: Person? = null
) {
    val personAndChildrenList: List<Person>
        get() = (parent?.let { listOf(it) } ?: emptyList()) + childProfiles
}

class AddChildProfilesViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {
    val startUserSessionUseCase: StartUserSessionUseCase = StartUserSessionUseCase(
        accountManager = di.direct.instance(),
    )
    private val _uiState = MutableStateFlow(
        AddChildProfilesUiState()
    )
    private var nextDestination: String =
        savedStateHandle[UstadView.ARG_NEXT] ?: ClazzListViewModel.DEST_NAME_HOME
    val repo: UmAppDatabase by di.onActiveEndpoint().instance()

    val uiState: Flow<AddChildProfilesUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { prev ->
            prev.copy(
                parent = accountManager.currentUserSession.person,
            )
        }
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.add_child_profiles),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.finish),
                    onClick = this@AddChildProfilesViewModel::onClickFinish,

                    )
            )
        }

        launchIfHasPermission(
            permissionCheck = {
                true
            }
        ) {
            async {
                loadEntity(
                    serializer = ListSerializer(Person.serializer()),
                    loadFromStateKeys = listOf(STATE_KEY_PERSONS),
                    onLoadFromDb = {
                        emptyList()

                    },
                    makeDefault = {
                        emptyList()
                    },
                    uiUpdate = {
                        _uiState.update { prev ->
                            prev.copy(childProfiles = it ?: emptyList())
                        }
                    }
                )
            }
            launch {
                //Handle text, module, and discussion topic (e.g. plain ChildProfile that does not
                // include any other entities)
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_PERSON).collect { result ->
                    val childProfileResult = result.result as? Person
                        ?: return@collect

                    val newChildProfileList =
                        _uiState.value.childProfiles.replaceOrAppend(childProfileResult) {
                            it.personUid == childProfileResult.personUid
                        }

                    updateChildProfileList(newChildProfileList)
                }
            }
        }

    }

    private suspend fun updateChildProfileList(
        newChildProfileList: List<Person>
    ) {
        _uiState.update { prev ->
            prev.copy(
                childProfiles = newChildProfileList
            )
        }

        savedStateHandle[STATE_KEY_PERSONS] = withContext(Dispatchers.Default) {
            json.encodeToString(
                ListSerializer(Person.serializer()),
                newChildProfileList
            )
        }
    }

    fun onClickFinish() {
        //if parent not added any child profiles then not showing any dialog
        if (_uiState.value.childProfiles.isNotEmpty()) {
            _uiState.update { prev ->
                prev.copy(
                    showProfileSelectionDialog = true,
                )
            }
        } else {
            onProfileSelected(accountManager.currentAccount.toPerson())
        }

    }

    fun onClickAddChileProfile() {
        navigateForResult(
            nextViewName = EditChildProfileViewModel.DEST_NAME,
            key = RESULT_KEY_PERSON,
            currentValue = null,
            args = buildMap {
                savedStateHandle[ARG_ENTITY_JSON]
            },
            serializer = Person.serializer(),
        )
    }

    fun onClickEditChileProfile(person: Person) {
        navigateForResult(
            nextViewName = EditChildProfileViewModel.DEST_NAME,
            key = RESULT_KEY_PERSON,
            serializer = Person.serializer(),
            args = buildMap {
                savedStateHandle[ARG_ENTITY_JSON]
            },
            currentValue = person,
        )
    }

    fun onClickDeleteChildProfile(person: Person) {
        viewModelScope.launch {
            updateChildProfileList(_uiState.value.childProfiles.filter {
                it.personUid != person.personUid
            })
        }
    }

    fun onDismissLangDialog() {
        _uiState.update { prev ->
            prev.copy(
                showProfileSelectionDialog = false,
            )
        }
    }

    fun onProfileSelected(profile: Person) {
        viewModelScope.launch {
            val goOptions = UstadMobileSystemCommon.UstadGoOptions(clearStack = true)

            if (_uiState.value.childProfiles.isNotEmpty()) {

                val effectiveDb = activeRepo ?: activeDb

                effectiveDb.personDao().insertListAsync(_uiState.value.childProfiles)

                val personParenJoinList = _uiState.value.childProfiles.map {
                    PersonParentJoin(
                        ppjMinorPersonUid = it.personUid,
                        ppjParentPersonUid = accountManager.currentAccount.personUid,
                        ppjStatus = PersonParentJoin.STATUS_APPROVED,
                        ppjApprovalTiemstamp = systemTimeInMillis()
                    )
                }
                _uiState.value.childProfiles.forEach {
                    if (it != profile && it != accountManager.currentUserSession.person) {
                        accountManager.addSession(it, accountManager.activeLearningSpace.url, null)
                    }
                }

                effectiveDb.personParentJoinDao().insertListAsync(personParenJoinList)
                if (profile != accountManager.currentUserSession.person) {
                    val sessionWithPersonAndLearningSpace =
                        accountManager.addSession(
                            profile,
                            accountManager.activeLearningSpace.url,
                            null
                        )
                    accountManager.currentUserSession = sessionWithPersonAndLearningSpace
                }
                navController.navigateToViewUri(
                    nextDestination.appendSelectedAccount(
                        profile.personUid,
                        LearningSpace(accountManager.activeLearningSpace.url)
                    ),
                    goOptions
                )

            } else {
                accountManager.currentUserSession = accountManager.currentUserSession

                navController.navigateToViewUri(
                    nextDestination.appendSelectedAccount(
                        profile.personUid,
                        LearningSpace(accountManager.activeLearningSpace.url)
                    ),
                    goOptions
                )
            }
        }


    }

    companion object {

        const val DEST_NAME = "AddChildProfile"

        const val RESULT_KEY_PERSON = "person"

        const val STATE_KEY_PERSONS = "persons"


    }
}