package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import org.kodein.di.DI

data class TimezoneListUiState(
    val timeZoneList: () -> PagingSource<Int, TimeZone> = { EmptyPagingSource() },
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

    private val timeZoneList = mutableListOf<TimeZone>()

    private val pagingSourceFactory: () -> PagingSource<Int, TimeZone> = {
        val searchText = _appUiState.value.searchState.searchText
        val searchWords = searchText.split(Regex("\\s+"))
        val newPagingSource = if(searchText.isBlank()){
            ListPagingSource(timeZoneList.toList())
        }else {
            ListPagingSource(timeZoneList.filter { timeZone ->
                searchWords.all { timeZone.id.contains(it, ignoreCase = true) }
            })
        }
        lastPagingSource?.invalidate()
        lastPagingSource = newPagingSource

        newPagingSource
    }

    private var lastPagingSource: PagingSource<Int, TimeZone>?= null

    init {
        _uiState.update { prev ->
            prev.copy(timeZoneList = pagingSourceFactory)
        }

        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MessageID.timezone),
                searchState = createSearchEnabledState()
            )
        }

        viewModelScope.launch {
            val allZones = withContext(Dispatchers.Default) {
                allTimeZones
            }

            timeZoneList.addAll(allZones)

            lastPagingSource?.invalidate()
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        lastPagingSource?.invalidate()
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