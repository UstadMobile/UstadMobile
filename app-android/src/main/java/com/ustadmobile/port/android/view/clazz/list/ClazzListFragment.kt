package com.ustadmobile.port.android.view.clazz.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.port.android.util.ext.defaultItemPadding
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.SortBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.port.android.view.composable.HtmlText
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.ustadmobile.core.R as CR


class ClazzListFragment(): UstadBaseMvvmFragment() {


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

    fun onClickFab() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            val uiState = viewModel.uiState.first()
//            val optionList = if(uiState.canAddNewCourse) {
//                listOf(
//                    BottomSheetOption(R.drawable.ic_add_black_24dp,
//                        requireContext().getString(CR.string.add_a_new_course), NEW_CLAZZ
//                    )
//                )
//            }else {
//                listOf()
//            } + listOf(
//                BottomSheetOption(R.drawable.ic_login_24px,
//                requireContext().getString(CR.string.join_existing_course), JOIN_CLAZZ
//                )
//            )
//
//            val sheet = OptionsBottomSheetFragment(optionList) {
//                when(it.optionCode) {
//                    NEW_CLAZZ -> viewModel.onClickAdd()
//                    JOIN_CLAZZ -> viewModel.onClickJoinExistingClazz()
//                }
//            }
//
//            sheet.show(childFragmentManager, sheet.tag)
//        }
    }

    companion object {

        const val NEW_CLAZZ = 2

        const val JOIN_CLAZZ = 3


        @JvmStatic
        val FOREIGNKEYADAPTER_COURSE = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.coursePictureDao.findByClazzUidAsync(foreignKey)?.coursePictureUri
            }
        }

    }


}

@Composable
private fun ClazzListScreen(viewModel: ClazzListViewModel) {
    val uiState: ClazzListUiState by viewModel.uiState.collectAsState(initial = ClazzListUiState())

    val context = LocalContext.current

    ClazzListScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickEntry,
        onClickSort =  {
            SortBottomSheetFragment(
                sortOptions = uiState.sortOptions,
                selectedSort = uiState.activeSortOrderOption,
                onSortOptionSelected = {
                    viewModel.onSortOrderChanged(it)
                }
            ).show(context.getContextSupportFragmentManager(), "SortOptions")
        },
        onClickFilterChip = viewModel::onClickFilterChip
    )
}



@Composable
private fun ClazzListScreen(
    uiState: ClazzListUiState = ClazzListUiState(),
    onClickClazz: (Clazz) -> Unit = {},
    onClickSort: () -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    val pager = remember(uiState.clazzList){
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.clazzList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),

        // 600 width of the smallest iPad,
        // subtracted 16 = horizontal padding & space between cards,
        // half of 584 is 292
        // card width = 292dp.
        columns = GridCells.Adaptive(292.dp)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListSortHeader(
                modifier = Modifier.defaultItemPadding(),
                activeSortOrderOption = uiState.activeSortOrderOption,
                enabled = uiState.fieldsEnabled,
                onClickSort = onClickSort
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            UstadListFilterChipsHeader(
                modifier = Modifier.defaultItemPadding(),
                filterOptions = uiState.filterOptions,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }


        /**
         * Note: Currently there is no direct support for LazyGrid with pagingsource.
         */
        items(
            lazyPagingItems.itemCount
        ) {
            ClazzListItem(
                clazz = lazyPagingItems[it],
                onClickClazz = onClickClazz
            )
        }

        //Host fragment thinks scroll bar behavior increases available height - need to compensate
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier =Modifier.height(176.dp))
        }
    }
}

@Composable
fun ClazzListItem(
    clazz: ClazzWithListDisplayDetails?,
    onClickClazz: (Clazz) -> Unit
){

    val role = RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == clazz?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.stringResource

    Card(
        modifier = Modifier
            .defaultItemPadding()
            .clickable {
                clazz?.also { onClickClazz(it) }
            },
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = clazz?.clazzName ?: "",
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                    )
                    HtmlText(
                        html = clazz?.clazzDesc ?: "",
                        htmlMaxLines = 2,
                    )
                }

//                if(role != null) {
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Badge,
//                            contentDescription = "",
//                        )
//                        Text(messageIdResource(id = role))
//                    }
//                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "",
                )
                Text(
                    text = stringResource(
                        CR.string.x_teachers_y_students,
                        clazz?.numTeachers ?: 0, clazz?.numStudents ?: 0,
                    )
                )
            }
        }
    }
}

@Composable
@Preview
fun ClazzListScreenPreview() {
    val uiStateVal = ClazzListUiState(
        clazzList = {
            ListPagingSource(
                listOf(
                    ClazzWithListDisplayDetails().apply {
                        clazzUid = 1
                        clazzName = "Class Name"
                        clazzDesc = "Class Description"
                        attendanceAverage = 0.3F
                        numTeachers = 3
                        numStudents = 2
                    },
                    ClazzWithListDisplayDetails().apply {
                        clazzUid = 2
                        clazzName = "Class Name"
                        clazzDesc = "Class Description"
                        attendanceAverage = 0.3F
                        numTeachers = 3
                        numStudents = 2
                    }
                )
            )
        },
    )

    MdcTheme {
        ClazzListScreen(uiStateVal)
    }
}