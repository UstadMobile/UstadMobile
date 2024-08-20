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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.util.UUID

@Composable
fun AddChildProfileScreen(viewModel: AddChildProfileViewModel) {
    val uiState: AddChildProfileUiState by viewModel.uiState.collectAsStateWithLifecycle(
        AddChildProfileUiState(), Dispatchers.Main.immediate
    )
    AddChildProfileScreen(
        uiState = uiState,
        onClickAddChild = viewModel::onClickAddChileProfile
    )
}

@Composable
fun AddChildProfileScreen(
    uiState: AddChildProfileUiState,
    onClickAddChild: () -> Unit = {},
) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item(key = "add_account") {
            UstadAddListItem(
                text = stringResource(MR.strings.child_profile),
                icon =  Icons.Default.Add,
                onClickAdd = { onClickAddChild() },
            )
        }


        items(
            uiState.childProfiles,
            key = {
                "${it.personUid} ${UUID.randomUUID()}"
            }
        ) { childProfile ->

            childProfileItem(childProfile = childProfile)
        }
    }


}

@Composable
fun childProfileItem(
    childProfile: Person
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
            )
        }
    )

}
