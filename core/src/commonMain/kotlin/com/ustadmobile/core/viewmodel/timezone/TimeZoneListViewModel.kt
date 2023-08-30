package com.ustadmobile.core.viewmodel.timezone

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.door.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import org.kodein.di.DI

data class TimezoneListUiState(
    val timeZoneList: List<TimeZone> = emptyList(),
)

class TimeZoneListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<TimezoneListUiState>(di, savedStateHandle, TimezoneListUiState(), DEST_NAME){

    private val allTimeZones: List<TimeZone> by lazy {
        val now = Clock.System.now()
        TimeZone.availableZoneIds.map {
            TimeZone.of(it)
        }.toList().sortedBy { it.offsetAt(now).totalSeconds }
    }

    private var searchUpdateJob: Job? = null

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MR.strings.timezone),
                searchState = createSearchEnabledState()
            )
        }

        viewModelScope.launch {
            val allTimeZonesVal = withContext(Dispatchers.Default) {
                allTimeZones
            }

            _uiState.update { prev ->
                prev.copy(allTimeZonesVal)
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        searchUpdateJob?.cancel()

        searchUpdateJob = viewModelScope.launch {
            val filteredList = withContext(Dispatchers.Default){
                val searchWords = searchText.split(Regex("\\s+"))
                allTimeZones.filter { timeZone ->
                    searchWords.all { timeZone.id.contains(it, ignoreCase = true) }
                }
            }
            _uiState.update { prev ->
                prev.copy(
                    timeZoneList = filteredList
                )
            }
        }
    }

    fun onClickEntry(entry: TimeZone) {
        finishWithResult(entry.id) //There is no such thing as detail view for timezone
    }

    override fun onClickAdd() {
        //do nothing - no adding timezones
    }

    companion object {

        const val DEST_NAME = "TimeZoneList"

    }
}