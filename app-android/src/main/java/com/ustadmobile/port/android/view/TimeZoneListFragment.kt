package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.util.ext.formattedString
import com.ustadmobile.core.viewmodel.TimeZoneListViewModel
import com.ustadmobile.core.viewmodel.TimezoneListUiState
import kotlinx.datetime.Clock
import java.util.*
import kotlinx.datetime.TimeZone as TimeZoneKt

class TimeZoneListFragment : UstadBaseMvvmFragment() {

    val viewModel: TimeZoneListViewModel by ustadViewModels(::TimeZoneListViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    TimeZoneListScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)

@Composable
fun TimeZoneListScreen(
    uiState: TimezoneListUiState,
    onListItemClick: (TimeZoneKt) -> Unit,
) {
    val pager = remember(uiState.timeZoneList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.timeZoneList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    val timeNow = Clock.System.now()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        items(
            items = lazyPagingItems,
            key = { it.id },
        ) { timeZone ->
            val timeZoneFormatted: String = remember(timeZone?.id) {
                timeZone?.formattedString(timeNow) ?: ""
            }

            ListItem(
                modifier = Modifier
                    .clickable { timeZone?.also { onListItemClick(it) } },
                text = { Text(timeZoneFormatted) }
            )
        }
    }
}

@Composable
fun TimeZoneListScreen(
    viewModel: TimeZoneListViewModel
) {
    val uiState: TimezoneListUiState by viewModel.uiState.collectAsState(TimezoneListUiState())
    TimeZoneListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry
    )
}
