package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import androidx.paging.compose.items
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.*

interface InviteWithLinkHandler{
    fun handleClickInviteWithLink()
}

class PersonListFragment() : UstadBaseMvvmFragment() {

    private val viewModel: PersonListViewModel by viewModels {
        UstadViewModelProviderFactory(di, this, arguments)
    }

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
                    PersonListScreen(viewModel)
                }
            }
        }
    }


}

@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel
) {
    val uiState: PersonListUiState by viewModel.uiState.collectAsState(PersonListUiState())

    PersonListScreen(
        uiState = uiState,
        onClickSort =  { },
        onListItemClick = viewModel::onClickEntry
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onClickSort: () -> Unit = {},
    onListItemClick: (PersonWithDisplayDetails) -> Unit = {}
){

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    val pager = remember(uiState.personList) {
        Pager(
            PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200)
        ) { uiState.personList }
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        item {
            UstadListSortHeader(
                activeSortOrderOption = uiState.sortOption,
                onClickSort = onClickSort
            )
        }
        
        items(
            items = lazyPagingItems,
            key = { it.personUid }
        ) {  person ->
            ListItem(
                modifier = Modifier
                    .clickable {
                         person?.also { onListItemClick(it) }
                    },
                text = { Text(text = "${person?.firstNames} ${person?.lastName}")},
                icon = {
                    Icon(
                        modifier = Modifier
                            .size(40.dp),
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null
                    )
                }
            )
        }

    }
}

@Preview
@Composable
private fun PersonEditPreview() {
    PersonListScreen(
        uiState = PersonListUiState(
            personList = ListPagingSource(listOf(
                PersonWithDisplayDetails().apply {
                    firstNames = "Ahmad"
                    lastName = "Ahmadi"
                    admin = true
                    personUid = 3
                }
            ))
        )
    )
}