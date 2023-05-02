package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.google.accompanist.themeadapter.appcompat.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListUiState
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.lib.db.entities.CourseTerminology

class CourseTerminologyListFragment: UstadBaseMvvmFragment() {

    private val viewModel: CourseTerminologyListViewModel by ustadViewModels(::CourseTerminologyListViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    CourseTerminologyListScreen(viewModel)
                }
            }
        }
    }


}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseTerminologyListScreen(
    uiState: CourseTerminologyListUiState,
    onClickAddNewItem: () -> Unit = { },
    onClickItem: (CourseTerminology) -> Unit = { },
) {
    val pager = remember(uiState.terminologyList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.terminologyList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        if(uiState.showAddItemInList) {
            item {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickAddNewItem()
                    },
                    text = { Text(stringResource(R.string.add_new_terminology)) },
                    icon = {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    }
                )
            }
        }

        items(
            items = lazyPagingItems,
            key = { it.ctUid },
        ) { terminology ->

            ListItem(
                modifier = Modifier.clickable {
                    terminology?.also { onClickItem(it) }
                },
                text = { Text(terminology?.ctTitle ?: "") },
                icon = {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            )
        }
    }
}

@Composable
fun CourseTerminologyListScreen(
    viewModel: CourseTerminologyListViewModel
) {
    val uiState: CourseTerminologyListUiState by viewModel.uiState.collectAsState(
        CourseTerminologyListUiState()
    )

    CourseTerminologyListScreen(
        uiState = uiState,
        onClickAddNewItem = viewModel::onClickAdd,
        onClickItem = viewModel::onClickEntry
    )

}


@Composable
@Preview
fun CourseTerminologyListScreenPreview() {
    CourseTerminologyListScreen(
        uiState = CourseTerminologyListUiState(
            showAddItemInList = true,
            terminologyList = {
                ListPagingSource(listOf(
                    CourseTerminology().apply {
                        ctUid = 1
                        ctTitle = "English"
                    }
                ))
            }
        )
    )
}
