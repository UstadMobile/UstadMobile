package com.ustadmobile.libuicompose.view.person.learningspacelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListUiState
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import dev.icerock.moko.resources.compose.stringResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun LearningSpaceListScreen ( viewModel: LearningSpaceListViewModel
) {
    val uiState: LearningSpaceListUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = LearningSpaceListUiState(),  context = Dispatchers.Main.immediate
    )

    LearningSpaceListScreen(
            uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickNext = viewModel::onClickNext,
        onSelectLearningSpace = viewModel::onSelectLearningSpace,
    )
}

@Composable
fun LearningSpaceListScreen(
    uiState: LearningSpaceListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickNext: () -> Unit = {},
    onSelectLearningSpace: (String) -> Unit = {},
) {

    val learningSpaceListPager = rememberDoorRepositoryPager(
        uiState.learningSpaceList, refreshCommandFlow
    )
    val learningSpaceListItems = learningSpaceListPager.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize(),
    )  {
        item {
            Spacer(Modifier.height(16.dp))
        }


        item {
            Text(
                stringResource(MR.strings.enter_link_manually),
                modifier = Modifier.defaultItemPadding()
                    .clickable { onClickNext() }

            )
        }
        Napier.e { "learningSpaceListItems "+learningSpaceListItems.itemCount }
        ustadPagedItems(
            pagingItems = learningSpaceListItems,
            key = { Pair(1, it.lsiUid) }
        ){ learningSpace ->
            ListItem (
                modifier = Modifier.clickable {
                    onSelectLearningSpace(learningSpace?.lsiUrl ?: "")
                },
                headlineContent = {
                    Text(text = learningSpace?.lsiUrl ?: "")
                }
            )
        }




    }

}