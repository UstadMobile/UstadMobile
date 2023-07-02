package com.ustadmobile.port.android.view.scopedgrant.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.compose.items
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.scopedgrant.list.ScopedGrantListUiState
import com.ustadmobile.core.viewmodel.scopedgrant.list.ScopedGrantListViewModel
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.composites.ScopedGrantEntityAndName
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.SortBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.UstadAddListItem
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import org.kodein.di.direct
import org.kodein.di.instance
import org.w3c.dom.Text

class ScopedGrantList2Fragment(): UstadBaseMvvmFragment() {



    private val viewModel: ScopedGrantListViewModel by ustadViewModels {
            di, savedStateHandle ->
        ScopedGrantListViewModel(di, savedStateHandle, requireDestinationViewName())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView((requireContext())).apply{

            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme{
                    ScopedGrantListScreen(viewModel)
                }
            }
        }
    }

}

@Composable
fun ScopedGrantListScreen(viewModel: ScopedGrantListViewModel){

    val uiState: ScopedGrantListUiState by viewModel.uiState.collectAsState(ScopedGrantListUiState())
    val context = LocalContext.current

    ScopedGrantListScreen(
        uiState = uiState,
        onClickSort = {
            SortBottomSheetFragment(
                sortOptions = uiState.sortOptions,
                selectedSort = uiState.sortOption,
                onSortOptionSelected = {
                    viewModel.onSortOrderChanged(it)
                }
            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
        onListItemClick = viewModel::onClickEntry,
        onClickAddNew = viewModel::onClickAdd
    )


}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScopedGrantListScreen(
    uiState: ScopedGrantListUiState,
    onClickSort: () -> Unit = {},
    onListItemClick: (ScopedGrantEntityAndName) -> Unit = {},
    onClickAddNew: () -> Unit = {},
){

    val pager = remember(uiState.scopeGrantList){
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.scopeGrantList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){
        item{
            UstadListSortHeader(
                modifier = Modifier
                    .defaultItemPadding()
                    .fillMaxWidth(),

                activeSortOrderOption = uiState.sortOption,
                onClickSort = onClickSort
            )
        }
        
        if(uiState.showAddItem){
            item{
                UstadAddListItem(
                    modifier = Modifier.testTag("add_new_scopedgrant"),
                    text = stringResource(R.string.add),
                    onClickAdd = onClickAddNew
                )
            }
        }

        items(
            items = lazyPagingItems,
            key = { it.scopedGrant?.sgUid ?: 0 }
        ){ scopedGrant ->

            val permissionsText = ""

            ListItem(
                modifier = Modifier
                    .clickable { 
                        scopedGrant?.also{ onListItemClick(it) }
                    },
                
                text = {
                    Text(text = scopedGrant?.name?:"")
                       },

                secondaryText = {
                    Text (text = permissionsText)
                },
                
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_lock_24),
                        contentDescription = ""
                    )
                }
                    
            )
        }
        
    }
}

//@Composable
//fun PermissionListField(permission: Long){
//    val enabledPermissions = MutableLiveData(
//        ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST.map{
//            it.toBitmaskFlag(permission)
//        }.filter { it.enabled })
//
//    Text(
//        text = enabledPermissions.value?.joinToString(", ") {
//            messageIdResource(id = it.messageId)
//        }.toString()
//    )
//}





@Preview
@Composable
private fun ScopedGrantListPreview(){
    ScopedGrantListScreen(
        uiState = ScopedGrantListUiState(
            scopeGrantList = {
                ListPagingSource(listOf(
                    ScopedGrantEntityAndName().apply {
                        name = "Varuna Singh"
                        scopedGrant = ScopedGrant().apply {

                        }
                    }
                ))
            }
        )
    )
}