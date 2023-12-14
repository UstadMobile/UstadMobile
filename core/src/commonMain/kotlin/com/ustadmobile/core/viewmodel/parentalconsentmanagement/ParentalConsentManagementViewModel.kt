package com.ustadmobile.core.viewmodel.parentalconsentmanagement

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.kodein.di.DI
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.Snack

data class ParentalConsentManagementUiState(

    val parentJoinAndMinor: PersonParentJoinAndMinorPerson? = null,

    val relationshipError: String? = null,

    val siteTerms: SiteTerms? = null,

    val fieldsEnabled: Boolean = false,

    val appName: String = "Ustad Mobile"

) {

    val relationshipVisible: Boolean
        get() = parentJoinAndMinor?.personParentJoin?.ppjParentPersonUid == 0L

    val consentButtonVisible: Boolean
        get() = parentJoinAndMinor?.personParentJoin?.ppjParentPersonUid == 0L

    val dontConsentButtonVisible: Boolean
        get() = parentJoinAndMinor?.personParentJoin?.ppjParentPersonUid == 0L
    val changeConsentButtonVisible: Boolean
        get() = parentJoinAndMinor?.personParentJoin?.ppjParentPersonUid.let {
            it != null && it != 0L
        }

    val consentStatusVisible: Boolean
        get() = parentJoinAndMinor?.personParentJoin?.ppjStatus.let {
            it != null && it != 0
        }

    val consentStatusText: StringResource?
        get() = when(parentJoinAndMinor?.personParentJoin?.ppjStatus) {
            PersonParentJoin.STATUS_APPROVED -> MR.strings.status_consent_granted
            PersonParentJoin.STATUS_REJECTED -> MR.strings.status_consent_denied
            else -> null
        }


    val changeConsentLabel: StringResource?
        get() = when(parentJoinAndMinor?.personParentJoin?.ppjStatus) {
            PersonParentJoin.STATUS_APPROVED -> MR.strings.revoke_consent
            PersonParentJoin.STATUS_REJECTED -> MR.strings.restore_consent
            else -> null
        }

}

class ParentalConsentManagementViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        ParentalConsentManagementUiState()
    )

    val uiState: Flow<ParentalConsentManagementUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.manage_parental_consent)
            )
        }

        viewModelScope.launch {
            loadEntity(
                serializer = PersonParentJoinAndMinorPerson.serializer(),
                onLoadFromDb = {
                    it.personParentJoinDao.findByUidWithMinorAsync(entityUidArg)
                },
                makeDefault = {
                    //Should never happen
                    null
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            parentJoinAndMinor = it
                        )
                    }
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onEntityChanged(
        personParentJoin: PersonParentJoin?
    ) {
        _uiState.update { prev ->
            prev.copy(
                parentJoinAndMinor = prev.parentJoinAndMinor?.copy(
                    personParentJoin = personParentJoin,
                )
            )
        }
    }

    private fun updateStatus(status: Int) {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(
                parentJoinAndMinor = prev.parentJoinAndMinor?.copy(
                    personParentJoin = prev.parentJoinAndMinor.personParentJoin?.shallowCopy {
                        ppjStatus = status
                        ppjParentPersonUid = activeUserPersonUid
                        ppjApprovalTiemstamp = systemTimeInMillis()
                    }
                )
            )
        }

        newState.parentJoinAndMinor?.personParentJoin?.also { personParentJoin ->
            viewModelScope.launch {
                activeRepo.personParentJoinDao.updateAsync(personParentJoin)
            }
        }

        snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.saved)))
    }

    fun onClickConsent() {
        updateStatus(PersonParentJoin.STATUS_APPROVED)
    }

    fun onClickDontConsent() {
        updateStatus(PersonParentJoin.STATUS_REJECTED)
    }

    fun onClickChangeConsent() {
        val currentStatus = _uiState.value.parentJoinAndMinor?.personParentJoin?.ppjStatus
            ?: return
        updateStatus(
            if(currentStatus == PersonParentJoin.STATUS_REJECTED)
                PersonParentJoin.STATUS_APPROVED
            else
                PersonParentJoin.STATUS_REJECTED
        )
    }


    companion object {

        const val DEST_NAME = "ParentalConsentManagement"

    }
}
