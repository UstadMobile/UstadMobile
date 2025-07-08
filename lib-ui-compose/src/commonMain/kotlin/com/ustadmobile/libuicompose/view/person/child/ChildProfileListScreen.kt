package com.ustadmobile.libuicompose.view.person.child

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListUiState
import com.ustadmobile.core.viewmodel.person.child.ChildProfileListViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.person.addaccount.ParentAndChildrenProfileSelectionDialog
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.util.UUID

@Composable
fun ChildProfileListScreen(viewModel: ChildProfileListViewModel) {
    val uiState: ChildProfileListUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ChildProfileListUiState(), Dispatchers.Main.immediate
    )
    ChildProfileListScreen(
        uiState = uiState,
        onClickAddChild = viewModel::onClickAddChileProfile,
        onClickEditChild = viewModel::onClickEditChileProfile,
        onClickDeleteChileProfile = viewModel::onClickDeleteChildProfile,
        onClickFinish = viewModel::onClickDone
    )

    if (uiState.showProfileSelectionDialog) {
        //As per https://developer.android.com/jetpack/compose/components/dialog
        ParentAndChildrenProfileSelectionDialog(
            onDismissRequest = viewModel::onDismissLangDialog,

            ) {
            Text(
                text = stringResource(MR.strings.which_profile_do_you_want_to_start),
                modifier = Modifier.padding(16.dp)
            )
            uiState.personAndChildrenList.forEach { profile ->
                ListItem(
                    modifier = Modifier.clickable { viewModel.onProfileSelected(profile) },
                    headlineContent = { Text(profile.fullName()) },
                )
            }
        }

    }
}

@Composable
fun ChildProfileListScreen(
    uiState: ChildProfileListUiState,
    onClickAddChild: () -> Unit = {},
    onClickFinish: () -> Unit = {},
    onClickEditChild: (Person) -> Unit = {},
    onClickDeleteChileProfile: (Person) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        UstadLazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            item(key = "add_account") {
                UstadAddListItem(
                    text = stringResource(MR.strings.child_profiles),
                    icon = Icons.Default.Add,
                    onClickAdd = { onClickAddChild() },
                )
            }

            items(
                uiState.childProfiles,
                key = {
                    "${it.personUid} ${UUID.randomUUID()}"
                }
            ) { childProfile ->
                childProfileItem(
                    childProfile = childProfile,
                    onClickEditChild = onClickEditChild,
                    onClickDeleteChileProfile = onClickDeleteChileProfile
                )
            }
        }

        Button(
            onClick = onClickFinish,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("finish"),
        ) {
            Text(stringResource(MR.strings.finish))
        }
    }

}

@Composable
fun childProfileItem(
    childProfile: Person,
    onClickEditChild: (Person) -> Unit = {},
    onClickDeleteChileProfile: (Person) -> Unit = {},
    ) {
    ListItem(
        leadingContent = {
            UstadPersonAvatar(
                personName = childProfile.fullName(),
            )
        },
        headlineContent = {
            Text(
                text = "${childProfile.firstNames} ${childProfile.lastName}",
                maxLines = 1,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.clickable { onClickDeleteChileProfile(childProfile) }
            )
        },
        modifier = Modifier.clickable {
            onClickEditChild(childProfile)
        }
    )

}
