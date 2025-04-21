package com.ustadmobile.libuicompose.view.person.passkey

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.credentials.UserPasskeyChallenge
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.person.passkey.PasskeyListUiState
import com.ustadmobile.core.viewmodel.person.passkey.PasskeyListViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow

@Composable
fun PasskeyListScreen(
    viewModel: PasskeyListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(PasskeyListUiState())

    PasskeyListScreen(
        viewModel = viewModel,
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        userChallenge = viewModel::findUserChallenge,
        onClickRevokePasskey = viewModel::onClickRevokePasskey

    )
}

@Composable
fun PasskeyListScreen(
    viewModel: PasskeyListViewModel,
    uiState: PasskeyListUiState = PasskeyListUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    userChallenge: (String) -> UserPasskeyChallenge,
    onClickRevokePasskey: (Long) -> Unit

) {
    if (uiState.showRevokePasskeyDialog) {
        showRevokePasskeyDialog(
            onDismissRequest = viewModel::onDismissRevokePasskeyDialog
        ) {
            UstadLazyColumn {
                item {
                    ListItem(

                        headlineContent = { Text(stringResource(MR.strings.delete_this_passkey)) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    MR.strings.loss_access_passkey_dialog,
                                )
                            )
                        }
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = viewModel::onDismissRevokePasskeyDialog
                        ) {
                            Text(stringResource(MR.strings.cancel))
                        }

                        OutlinedButton(
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                .testTag("delete"),
                            onClick = viewModel::revokePasskey
                        ) {
                            Text(stringResource(MR.strings.delete))
                        }
                    }
                }

            }


        }
    }

    val passkeyListPager = rememberDoorRepositoryPager(
        uiState.passkeys, refreshCommandFlow
    )
    val passkeyListItems = passkeyListPager.lazyPagingItems


    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {


        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(MR.strings.secure_passkey_detail_text))
                }
            )
        }
        ustadPagedItems(
            pagingItems = passkeyListItems,
            key = { it.personPasskeyUid }
        ) { passkeyListItem ->
            val userChallengeItem = userChallenge(passkeyListItem?.ppChallengeString.toString())
            PasskeyListItem(
                item = passkeyListItem,
                onClickRevokePasskey = onClickRevokePasskey,
                userChallenge = userChallengeItem
            )
        }
    }
}


@Composable
fun PasskeyListItem(
    item: PersonPasskey?,
    onClickRevokePasskey: (Long) -> Unit,
    userChallenge: UserPasskeyChallenge
) {


    val createdDate = rememberFormattedDateTime(
        timeInMillis = userChallenge.usStartTime,
        timeZoneId = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    ListItem(
        modifier =
            Modifier.clickable {
            },
        headlineContent = {
            Text(
                text = userChallenge.username,
                maxLines = 1,
            )
        },
        leadingContent = {
            Icon(
                Icons.Default.Security,
                contentDescription = "delete"
            )
        },
        supportingContent = {
            Text(
                text = "${stringResource(MR.strings.created)} $createdDate",
                maxLines = 1,
            )
        },
        trailingContent = {
            IconButton(
                onClick = { onClickRevokePasskey(item?.personPasskeyUid ?: 0) },
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "delete"
                )
            }

        },
    )
}


@Composable
fun showRevokePasskeyDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        //As per https://developer.android.com/jetpack/compose/components/dialog
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            content()
        }
    }
}