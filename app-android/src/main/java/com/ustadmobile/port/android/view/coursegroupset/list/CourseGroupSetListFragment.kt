package com.ustadmobile.port.android.view.coursegroupset.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.SortBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment

class CourseGroupSetListFragment(): UstadBaseMvvmFragment(){

    private val viewModel by ustadViewModels(::CourseGroupSetListViewModel)

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
                    CourseGroupSetListScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CourseGroupSetListScreen(
    viewModel: CourseGroupSetListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(CourseGroupSetListUiState())
    val context = LocalContext.current

    CourseGroupSetListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
        onClickSort =  {
            SortBottomSheetFragment(
                sortOptions = uiState.sortOptions,
                selectedSort = uiState.sortOption,
                onSortOptionSelected = {
                    viewModel.onSortOptionChanged(it)
                }
            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseGroupSetListScreen(
    uiState: CourseGroupSetListUiState,
    onClickEntry: (CourseGroupSet) -> Unit = {},
    onClickSort: () -> Unit = {},
) {
    val pager = remember(uiState.courseGroupSets) {
        Pager(
            pagingSourceFactory = uiState.courseGroupSets,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
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
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.sortOption,
                onClickSort =   onClickSort
            )
        }

        items(
            items = lazyPagingItems,
            key = { it.cgsUid }
        ) { courseGroupSet ->
            ListItem(
                modifier = Modifier.clickable {
                    courseGroupSet?.also(onClickEntry)
                },
                text = {
                    Text(courseGroupSet?.cgsName ?: "")
                },
            )
        }
    }
}

@Composable
@Preview
fun CourseGroupSetListScreenPreview() {
    CourseGroupSetListScreen(
        uiState = CourseGroupSetListUiState(
            courseGroupSets = {
                ListPagingSource(listOf(
                    CourseGroupSet().apply {
                        cgsName = "Assignment groups"
                        cgsUid = 1
                    }
                ))
            }
        )
    )
}
