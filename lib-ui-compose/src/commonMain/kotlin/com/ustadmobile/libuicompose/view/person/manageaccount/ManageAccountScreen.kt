package com.ustadmobile.libuicompose.view.person.manageaccount

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.person.manageaccount.ManageAccountUiState
import com.ustadmobile.core.viewmodel.person.manageaccount.ManageAccountViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.coroutines.Dispatchers

@Composable
fun ManageAccountScreen(
    viewModel: ManageAccountViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        ManageAccountUiState(), Dispatchers.Main.immediate
    )

    ManageAccountScreen(
        uiState = uiState,
        onCreatePasskeyClick = viewModel::onCreatePasskeyClick,
        onClickManagePasskey = viewModel::onClickManagePasskey,
        onClickChangePassword = viewModel::navigateToEditAccount,

        )

}

@Composable
fun ManageAccountScreen(
    uiState: ManageAccountUiState,
    onCreatePasskeyClick: () -> Unit = {},
    onClickManagePasskey: () -> Unit = {},
    onClickChangePassword: () -> Unit = {},

) {
    UstadLazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            uiState.errorText?.also { errorText ->
                Text(color = MaterialTheme.colorScheme.error, text = errorText)
            }
        }
        item(key = "name") {
            ListItem(
                headlineContent = {
                    Text(text = uiState.personName)
                },
                supportingContent = {
                    Text(text = stringResource(MR.strings.name_key))
                }

            )
        }
        item(key = "username") {
            ListItem(
                headlineContent = {
                    Text(text = uiState.personUsername)
                },
                supportingContent = {
                    Text(text = stringResource(MR.strings.username))
                }

            )
        }
        if (uiState.passkeySupported){
            item(key = "security") {
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(MR.strings.security))
                    }
                )
            }
            if (uiState.showCreatePasskey) {
                item(key = "create_passkey") {
                    ListItem(
                        trailingContent = {
                            Icon(Icons.Default.Security, contentDescription = null)
                        },
                        headlineContent = {
                            Text(stringResource(MR.strings.sign_in_faster_next_time))
                        },
                        supportingContent = {
                            Text(stringResource(MR.strings.secure_passkey_detail_text))
                        }
                    )

                    OutlinedButton(
                        modifier = Modifier.fillParentMaxWidth().padding(start = 10.dp, end = 10.dp),
                        onClick = onCreatePasskeyClick,
                    ) {
                        Text(stringResource(MR.strings.create_passkey))
                    }
                }
            }
            if (!uiState.showCreatePasskey) {
                item(key = "manage_passkey") {
                    ListItem(

                        leadingContent = {
                            Icon(Icons.Default.Security, contentDescription = null)
                        },
                        headlineContent = {
                            Text(
                                text = "${uiState.passkeyCount} ${stringResource(MR.strings.passkeys)}",
                                maxLines = 1,
                            )
                        },
                        supportingContent = {

                            Text(
                                text = stringResource(MR.strings.passkeys),

                                )

                        },
                        trailingContent = {
                            Text(
                                modifier = Modifier.clickable { onClickManagePasskey() },
                                text = stringResource(MR.strings.manage),
                            )
                        }
                    )
                }
            }
        }

        item(key = "manage_password") {

            val lastUpdatedDate = uiState.personAuth?.pauthLct?.let {
                rememberFormattedDateTime(
                    timeInMillis = it,
                    timeZoneId = kotlinx.datetime.TimeZone.currentSystemDefault().id,
                )
            }
            ListItem(

                leadingContent = {
                    Icon(Icons.Default.Password, contentDescription = null)
                },
                headlineContent = {
                    Text(
                        text = stringResource(MR.strings.password),
                        maxLines = 1,
                    )
                },
                supportingContent = {

                    Text(
                        text = "${stringResource(MR.strings.last_updated)} $lastUpdatedDate",

                        )

                },
                trailingContent = {
                    Text(
                        modifier = Modifier.clickable { onClickChangePassword() },
                        text = stringResource(MR.strings.change_password),
                    )
                }
            )
        }

        item(key = "bottom_divider") {
            Divider(thickness = 1.dp)
        }

    }
}
