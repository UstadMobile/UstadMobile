package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.*
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.port.android.util.ext.defaultAvatarSize
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar
import com.ustadmobile.core.R as CR

interface InviteWithLinkHandler{
    fun handleClickInviteWithLink()
}

class PersonListFragment() : UstadBaseMvvmFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

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

    val context = LocalContext.current

    PersonListScreen(
        uiState = uiState,
        onClickSort =  {
            SortBottomSheetFragment(
                sortOptions = uiState.sortOptions,
                selectedSort = uiState.sortOption,
                onSortOptionSelected = {
                    viewModel.onSortOrderChanged(it)
                }
            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    onClickSort: () -> Unit = {},
    onListItemClick: (PersonWithDisplayDetails) -> Unit = {},
    onClickAddNew: () -> Unit = {},
){

    // As per
    // https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#collectaslazypagingitems
    // Must provide a factory to pagingSourceFactory that will
    // https://issuetracker.google.com/issues/241124061
    val pager = remember(uiState.personList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.personList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        item {
            UstadListSortHeader(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth(),
                activeSortOrderOption = uiState.sortOption,
                onClickSort = onClickSort
            )
        }

        if(uiState.showAddItem) {
            item {
                UstadAddListItem(
                    modifier = Modifier.testTag("add_new_person"),
                    text = stringResource(CR.string.add_a_new_person),
                    onClickAdd = onClickAddNew
                )
            }
        }
        
    items(
        count = lazyPagingItems.itemCount,
        key = lazyPagingItems.itemKey(key = { it.personUid }),
        contentType = lazyPagingItems.itemContentType()
    ) { index ->
        val item = lazyPagingItems[index]
        ListItem(
            modifier = Modifier
                .clickable {
                    item?.also { onListItemClick(it) }
                },
            text = { Text(text = "${item?.firstNames} ${item?.lastName}") },
            icon = {
                UstadPersonAvatar(
                    item?.personUid ?: 0,
                    modifier = Modifier.defaultAvatarSize(),
                )
            },
        )
        }

    }
}

@Preview
@Composable
private fun PersonEditPreview() {
    PersonListScreen(
        uiState = PersonListUiState(
            personList = {
                ListPagingSource(listOf(
                    PersonWithDisplayDetails().apply {
                        firstNames = "Ahmad"
                        lastName = "Ahmadi"
                        admin = true
                        personUid = 3
                    }
                ))
            }
        )
    )
}