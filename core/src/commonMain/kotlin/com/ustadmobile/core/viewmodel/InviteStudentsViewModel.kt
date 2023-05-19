package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.util.ext.requireHttpPrefix
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.verifySite
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.siteenterlink.SiteEnterLinkViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.kodein.di.DI


data class InviteStudentsUiState(

    val fieldsEnabled: Boolean = true,

    val studentsList: List<Person> = emptyList(),

    val classInvitationLink: String = "",

)

class InviteStudentsViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(InviteStudentsUiState(fieldsEnabled = false))

    val uiState: Flow<InviteStudentsUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(MessageID.invite_link_desc, MessageID.invite_link_desc),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickInvite
                )
            )
        }

        viewModelScope.launch {
            loadEntity<Schedule>(
                serializer = Schedule.serializer(),
                makeDefault = {
                    Schedule().apply {
                        scheduleUid = activeDb.doorPrimaryKeyManager.nextIdAsync(Schedule.TABLE_ID)
                        scheduleActive = true
                        scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                    }
                },
                onLoadFromDb = { null }, //Does not load from database
                uiUpdate = { schedule ->
//                    _uiState.update { prev ->
//                        prev.copy(entity = schedule)
//                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onClickAddStudent(email: String) {
        _uiState.update {
            it.copy(fieldsEnabled = false)
        }

        loadingState = LoadingUiState.INDETERMINATE
    }

    fun onClickInvite() {

    }

    companion object {

        const val DEST_NAME = "InviteStudents"

    }

}
