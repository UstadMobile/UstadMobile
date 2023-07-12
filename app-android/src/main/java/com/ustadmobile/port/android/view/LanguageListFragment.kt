package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemLanguageListBinding
import com.ustadmobile.core.controller.LanguageListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.core.viewmodel.LanguageListUiState
import com.ustadmobile.core.viewmodel.LanguageListViewModel
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.view.composable.UstadDetailField
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class LanguageListFragment: UstadBaseMvvmFragment() {

    private val viewModel: LanguageListViewModel by ustadViewModels(::LanguageListViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    LanguageListScreenForViewModel(viewModel)
                }
            }
        }
    }
}

@Composable
private fun LanguageListScreenForViewModel(
    viewModel: LanguageListViewModel
){
    val uiState: LanguageListUiState by viewModel.uiState.collectAsState(initial = LanguageListUiState())
    LanguageListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onListItemClick,
        onClickSort = viewModel::onClickSort
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageListScreen(
    uiState: LanguageListUiState = LanguageListUiState(),
    onListItemClick: (Language) -> Unit = {},
    onClickSort: () -> Unit = {}
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        item {
            UstadListSortHeader(
                activeSortOrderOption = uiState.sortOrder,
                onClickSort = onClickSort
            )
        }

        items(
            uiState.languageList,
            key = {
                it.langUid
            }
        ){  language ->
            ListItem(
                modifier = Modifier
                    .clickable {
                        onListItemClick(language)
                    },
                text = { Text(text = language.name ?: "")},
                secondaryText = { Text(text = "${language.iso_639_1_standard} / ${language.iso_639_2_standard}")}
            )
        }
    }
}

@Composable
@Preview
fun LanguageListScreenPreview() {
    MdcTheme{
        LanguageListScreen()
    }
}