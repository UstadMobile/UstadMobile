@file:OptIn(ExperimentalMaterial3Api::class)

package com.ustadmobile.libuicompose.view.accountlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.viewmodel.accountlist.AccountListUiState
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.libuicompose.components.UstadAddListItem
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import kotlinx.coroutines.Dispatchers

@Composable
fun AccountListScreen(
    viewModel: AccountListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        AccountListUiState(), Dispatchers.Main.immediate
    )

    AccountListScreen(uiState = uiState,
        onAccountListItemClick = viewModel::onClickAccount,
        onDeleteListItemClick = viewModel::onClickDeleteAccount,
        onAddItem = viewModel::onClickAddAccount,
        onLogoutClick = viewModel::onClickLogout,
        onMyProfileClick = viewModel::onClickProfile,
        onClickOpenLicenses = viewModel::onClickOpenLicenses,
        onClickAppShare = viewModel::onToggleShareAppOptions    )

    if (uiState.shareAppOptionVisible) {
        ModalBottomSheet(onDismissRequest = { viewModel.onToggleShareAppOptions() }) {
            UstadBottomSheetOption(modifier = Modifier.clickable {
                viewModel.onClickAppShare(false)
            },
                headlineContent = { Text(stringResource(MR.strings.send_apk_file)) },
                leadingContent = { Icon(Icons.Outlined.Android, contentDescription = null) })

            UstadBottomSheetOption(modifier = Modifier.clickable {
                viewModel.onClickAppShare(true)
            },
                headlineContent = { Text(stringResource(MR.strings.send_app_link)) },
                leadingContent = {
                    Icon(Icons.Outlined.Link, contentDescription = null)
                })
        }
    }
}

@Composable
fun AccountListScreen(
    uiState: AccountListUiState,
    onAccountListItemClick: (UserSessionWithPersonAndEndpoint) -> Unit = {},
    onDeleteListItemClick: (UserSessionWithPersonAndEndpoint) -> Unit = {},
    onClickOpenLicenses: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onMyProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onClickAppShare: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    UstadLazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (uiState.headerAccount != null) {
            item(key = "header_account") {
                AccountListItem(
                    account = uiState.headerAccount
                )
            }

            item(key = "header_buttons") {
                Row(
                    modifier = Modifier.padding(start = 72.dp, bottom = 16.dp)
                ) {
                    if (uiState.myProfileButtonVisible) {
                        OutlinedButton(
                            onClick = onMyProfileClick,
                        ) {
                            Text(stringResource(MR.strings.my_profile))
                        }
                    }

                    OutlinedButton(
                        modifier = Modifier.padding(start = 10.dp).testTag("logout_button"),
                        onClick = onLogoutClick,
                    ) {
                        Text(stringResource(MR.strings.logout))
                    }
                }
            }

            item(key = "header_divider") {
                Divider(thickness = 1.dp)
            }
        }

        items(uiState.accountsList, key = {
            "${it.person.personUid}@${it.endpoint}"
        }) { account ->
            AccountListItem(
                account = account,
                onClickAccount = { selectedAccount ->
                    selectedAccount?.also(onAccountListItemClick)
                },
                trailing = {
                    IconButton(onClick = {
                        onDeleteListItemClick(account)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(MR.strings.remove),
                        )
                    }
                },
            )
        }

        item(key = "add_account") {
            UstadAddListItem(
                text = stringResource(MR.strings.add_another_account),
                onClickAdd = onAddItem,
            )
        }

        item(key = "share_app") {

            ListItem(
                modifier = Modifier.clickable { onClickAppShare() },
                leadingContent = {
                    Icon(Icons.Default.Share, contentDescription = null)
                },
                headlineContent = { Text(stringResource(MR.strings.share_app)) },
            )
        }

        item(key = "bottom_divider") {
            Divider(thickness = 1.dp)
        }

        item(key = "about") {
            ListItem(modifier = if (uiState.showPoweredBy) {
                Modifier.clickable {
                    uriHandler.openUri("https://www.ustadmobile.com/")
                }
            } else {
                Modifier
            },
                headlineContent = { Text(stringResource(MR.strings.version)) },
                supportingContent = {
                    val versionStr = if (uiState.showPoweredBy) {
                        "${uiState.version} ${stringResource(MR.strings.powered_by)}"
                    } else {
                        uiState.version
                    }

                    Text(text = versionStr)
                })
        }

        item(key = "open_licenses") {
            ListItem(
                modifier = Modifier.clickable {
                    onClickOpenLicenses()
                },
                headlineContent = {
                    Text(stringResource(MR.strings.licenses))
                },
            )
        }
    }
}

@Composable
fun AccountListItem(
    account: UserSessionWithPersonAndEndpoint?,
    trailing: @Composable (() -> Unit)? = null,
    onClickAccount: ((UserSessionWithPersonAndEndpoint?) -> Unit)? = null
) {
    ListItem(modifier = if (onClickAccount != null) {
        Modifier.clickable {
            onClickAccount(account)
        }
    } else {
        Modifier
    }, leadingContent = {
        UstadPersonAvatar(
            personName = account?.person?.fullName(),
            pictureUri = account?.personPicture?.personPictureThumbnailUri,
        )
    }, headlineContent = {
        Text(
            text = "${account?.person?.firstNames} ${account?.person?.lastName}",
            maxLines = 1,
        )
    }, supportingContent = {
        Row {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                Modifier.size(16.dp)
            )
            Text(
                text = account?.person?.username ?: "",
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                Modifier.size(16.dp)
            )
            Text(
                text = account?.endpoint?.url ?: "",
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }, trailingContent = trailing
    )
}