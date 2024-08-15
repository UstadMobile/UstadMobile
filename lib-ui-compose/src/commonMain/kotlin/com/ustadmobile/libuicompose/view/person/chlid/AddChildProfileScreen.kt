package com.ustadmobile.libuicompose.view.person.chlid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileUiState
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AddChildProfileScreen(viewModel: AddChildProfileViewModel) {
    val uiState: AddChildProfileUiState by viewModel.uiState.collectAsStateWithLifecycle(
        AddChildProfileUiState(), Dispatchers.Main.immediate)
    AddChildProfileScreen(
        uiState = uiState,
        onClickAddChild=viewModel::onClickAddChileProfile
    )
}

@Composable
fun AddChildProfileScreen(
    uiState: AddChildProfileUiState,
    onClickAddChild: () -> Unit = {},
) {
    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize()
    )  {


            ListItem(
                modifier = Modifier
                    .testTag("add_child_profile")
                    .clickable { onClickAddChild() },
                headlineContent = { Text(stringResource(MR.strings.child_profile)) },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        }

}
