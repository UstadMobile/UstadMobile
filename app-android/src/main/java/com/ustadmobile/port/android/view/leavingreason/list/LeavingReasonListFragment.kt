package com.ustadmobile.port.android.view.leavingreason.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListUiState
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListViewModel
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.toughra.ustadmobile.R

class LeavingReasonListFragment : UstadBaseMvvmFragment() {

    private val viewModel: LeavingReasonListViewModel by ustadViewModels(::LeavingReasonListViewModel)

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
                    LeavingReasonListScreen(viewModel)
                }
            }
        }
    }

}

@Composable
fun LeavingReasonListScreen(
    viewModel: LeavingReasonListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(LeavingReasonListUiState())

    LeavingReasonListScreen(
        uiState = uiState,
        onEditLeavingReason = viewModel::onEditLeavingReason,
        onClickAddLeavingReason = viewModel::onClickAdd,
    )
}

@Composable
fun LeavingReasonListScreen(
    uiState: LeavingReasonListUiState = LeavingReasonListUiState(),
    onEditLeavingReason: (LeavingReason) -> Unit = {},
    onClickAddLeavingReason: () -> Unit = {},
) {

    val leavingReasonListPager = remember(uiState.leavingReasonList) {
        Pager(
            pagingSourceFactory = uiState.leavingReasonList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }

    val leavingReasonListItems = leavingReasonListPager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

        item {
            UstadAddListItem(
                text = stringResource(R.string.add_leaving_reason),
                onClickAdd = onClickAddLeavingReason
            )
        }

        items(
            items = leavingReasonListItems,
            key = { it.leavingReasonUid },
        ){ pendingLeavingReason ->
            LeavingReasonListItem(
                leavingReason = pendingLeavingReason,
                onEditLeavingReason = onEditLeavingReason
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LeavingReasonListItem(
    leavingReason: LeavingReason?,
    onEditLeavingReason: (LeavingReason) -> Unit,
){

    ListItem (
        icon = {
            Spacer(modifier = Modifier.width(24.dp))
        },
        text = {
            Text(text = leavingReason?.leavingReasonTitle ?: "")
        },
        trailing = {
            IconButton(
                onClick = { leavingReason?.also { onEditLeavingReason(it) } }
            ){
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_leaving_reason)
                )
            }
        }
    )
}

@Composable
@Preview
fun LeavingReasonListScreenPreview() {
    val uiStateVal = LeavingReasonListUiState()

    MdcTheme {
        LeavingReasonListScreen(
            uiState = uiStateVal
        )
    }
}