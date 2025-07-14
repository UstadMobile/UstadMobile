package com.ustadmobile.core.viewmodel.report.filteredit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.ReportConditionFilterOptions
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class ReportFilterEditUiState(
    val filters: ReportFilter3? = ReportFilter3(),
    val filterConditionOptions: ReportConditionFilterOptions? = null,
)

class ReportFilterEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ReportFilterEditUiState())
    val uiState: StateFlow<ReportFilterEditUiState> = _uiState

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = systemImpl.getString(MR.strings.edit_filters)

        // First check if we're editing an existing filter
        val existingFilterJson = savedStateHandle[ARG_EXISTING_FILTER]
        if (existingFilterJson != null) {
            handleExistingFilter(existingFilterJson)
        } else {
            handleNewFilter(savedStateHandle)
        }

        setupAppUiState(title)
    }

    private fun handleExistingFilter(existingFilterJson: String) {
        try {
            val existingFilter =
                json.decodeFromString(ReportFilter3.serializer(), existingFilterJson)
            _uiState.value = ReportFilterEditUiState(
                filters = existingFilter,
                filterConditionOptions = getConditionOptionsForField(existingFilter.reportFilterField)
            )
        } catch (e: Exception) {
            _uiState.value = ReportFilterEditUiState(
                filters = null,
                filterConditionOptions = null
            )
        }
    }

    private fun handleNewFilter(savedStateHandle: UstadSavedStateHandle) {
        val tempFilterUid = savedStateHandle[ARG_TEMP_FILTER_UID]?.toInt()
        val seriesUid = savedStateHandle[ARG_REPORT_SERIES_UID]?.toInt()

        // Assign the temp UID to the new filter
        _uiState.value = if (tempFilterUid != null && seriesUid != null) {
            ReportFilterEditUiState(
                filters = ReportFilter3(
                    reportFilterUid = tempFilterUid,
                    reportFilterSeriesUid = seriesUid
                )
            )
        } else {
            ReportFilterEditUiState(filters = null)
        }
    }

    private fun setupAppUiState(title: String) {
        _appUiState.update {
            AppUiState(
                title = title,
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.done),
                    onClick = ::onClickSave
                )
            )
        }
    }

    fun onClickSave() {
        val currentFilter = uiState.value.filters
        if (currentFilter != null && isValidFilter(currentFilter)) {
            finishWithResult(
                ReportEditViewModel.DEST_NAME,
                entityUid = currentFilter.reportFilterUid.toLong(),
                result = currentFilter
            )
        }
    }

    private fun isValidFilter(filter: ReportFilter3): Boolean {
        return filter.reportFilterField != null &&
                filter.reportFilterCondition != null &&
                filter.reportFilterValue?.isNotBlank() == true
    }

    fun onEntityChanged(value: ReportFilter3?) {
        val updatedFilter = value?.copy(
            reportFilterSeriesUid = _uiState.value.filters?.reportFilterSeriesUid ?: 0
        )

        _uiState.update { currentState ->
            currentState.copy(
                filters = updatedFilter,
                filterConditionOptions = getConditionOptionsForField(updatedFilter?.reportFilterField)
            )
        }

        scheduleEntityCommitToSavedState(
            entity = updatedFilter,
            serializer = ReportFilter3.serializer(),
            commitDelay = 200
        )
    }

    private fun getConditionOptionsForField(field: FilterType?): ReportConditionFilterOptions? {
        return when (field) {
            FilterType.PERSON_AGE -> ReportConditionFilterOptions.AgeConditionFilter()
            FilterType.PERSON_GENDER -> ReportConditionFilterOptions.GenderConditionFilter()
            else -> null
        }
    }

    companion object {
        const val DEST_NAME = "ReportFilterEdit"
        const val ARG_REPORT_SERIES_UID = "reportSeriesUid"
        const val ARG_TEMP_FILTER_UID = "tempFilterUid"
        const val ARG_EXISTING_FILTER = "existingFilter"
    }
}