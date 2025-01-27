package com.ustadmobile.libuicompose.view.clazz.inviteViaContact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.PendingInviteUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.PendingInviteViewModel
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.emptyFlow


@Composable
fun PendingInviteScreen(
    viewModel: PendingInviteViewModel
) {

    val uiState by viewModel.uiState.collectAsState(PendingInviteUiState())

    PendingInviteScreen(
        uiState,
        onClickRevokeInvite = { viewModel.onClickRevokeInvite(it) },
        onClickResendInvite = { viewModel.onClickResendInvite(it) },
    )
}


@Composable
fun PendingInviteScreen(
    uiState: PendingInviteUiState = PendingInviteUiState(),
    onClickRevokeInvite:(String) -> Unit,
    onClickResendInvite:(String) -> Unit
) {
    val refreshFlow = remember {
        emptyFlow<RefreshCommand>()
    }

    val result = rememberDoorRepositoryPager(
        uiState.pendingInviteList, refreshFlow
    )

    val pagingItems = result.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = pagingItems,
            key = { it.ciUid ?: 0 }
        ) { item: ClazzInvite? ->
            ListItem(
                modifier =
                Modifier.clickable {
                },

                headlineContent = {
                    Text(
                        text = item?.inviteContact.toString(),
                        maxLines = 1,
                    )
                },
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = {
                                item?.inviteContact?.let { onClickResendInvite(it) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(MR.strings.reminder),
                            )
                        }
//
                        IconButton(
                            onClick = {
                                item?.inviteContact?.let { onClickRevokeInvite(it) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(MR.strings.remove),
                            )
                        }
                    }
                },
            )
        }
    }


}




