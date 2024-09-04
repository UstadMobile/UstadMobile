package com.ustadmobile.libuicompose.view.person.learningspacelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListUiState
import com.ustadmobile.core.viewmodel.person.learningspacelist.LearningSpaceListViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun LearningSpaceListScreen ( viewModel: LearningSpaceListViewModel
) {
    val uiState: LearningSpaceListUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = LearningSpaceListUiState(),  context = Dispatchers.Main.immediate
    )

    LearningSpaceListScreen(
            uiState = uiState,
        onClickNext = viewModel::onClickNext,
    )
}

@Composable
fun LearningSpaceListScreen(
    uiState: LearningSpaceListUiState,
    onClickNext: () -> Unit = {},
) {


    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {
        Spacer(Modifier.height(16.dp))



        Text(
            stringResource(MR.strings.enter_link_manually),
            modifier = Modifier.defaultItemPadding()
                .clickable { onClickNext() }

        )



    }

}