package com.ustadmobile.libuicompose.view.person.chlid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileUiState
import com.ustadmobile.core.viewmodel.person.child.AddChildProfileViewModel
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.view.person.addaccount.ParentAndChildrenProfileSelectionDialog
import com.ustadmobile.libuicompose.view.settings.SettingsDialog
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel
import java.util.UUID

@Composable
fun AddChildProfileScreen(viewModel: AddChildProfileViewModel) {
    val uiState: AddChildProfileUiState by viewModel.uiState.collectAsStateWithLifecycle(
        AddChildProfileUiState(), Dispatchers.Main.immediate
    )
    AddChildProfileScreen(
        uiState = uiState,
        onClickAddChild = viewModel::onClickAddChileProfile,
        onClickEditChild = viewModel::onClickEditChileProfile,
        onClickDeleteChileProfile = viewModel::onClickDeleteChildProfile
    )

    if (uiState.showProfileSelectionDialog) {
        //As per https://developer.android.com/jetpack/compose/components/dialog
        ParentAndChildrenProfileSelectionDialog(
            onDismissRequest = viewModel::onDismissLangDialog,

            ) {
            Text(text = stringResource(MR.strings.which_profile_do_you_want_to_start), modifier = Modifier.padding(16.dp))
            uiState.profileIncludingParentAndChildren.forEach { profile ->
                ListItem(modifier = Modifier.clickable { viewModel.onProfileSelected(profile) },
                    headlineContent = { Text(profile.fullName()) },)
            }
        }

    }
}

@Composable
fun AddChildProfileScreen(
    uiState: AddChildProfileUiState,
    onClickAddChild: () -> Unit = {},
    onClickEditChild: (Person) -> Unit = {},
    onClickDeleteChileProfile: (Person) -> Unit = {},
) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item(key = "add_account") {
            UstadAddListItem(
                text = stringResource(MR.strings.child_profile),
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
